package it.bologna.ausl.masterchef.workers;

import com.mongodb.MongoTimeoutException;
import it.bologna.ausl.masterchef.Chef;
import static it.bologna.ausl.masterchef.Launcher.config;
import it.bologna.ausl.masterchef.utils.Base64Coder;
import it.bologna.ausl.masterchef.errors.SendToParerErrorDetails;
import it.bologna.ausl.masterchef.errors.WorkerException;
import it.bologna.ausl.masterchef.utils.HttpCallResponse;
import static it.bologna.ausl.masterchef.utils.UtilityFunctions.getTokenFromInternauta;
import static it.bologna.ausl.masterchef.utils.UtilityFunctions.httpCallWithHeaders;
import it.bologna.ausl.mongowrapper.MongoWrapper;
import it.bologna.ausl.mongowrapper.exceptions.MongoWrapperException;
import it.bologna.ausl.riversamento.sender.Pacco;
import it.bologna.ausl.riversamento.sender.PaccoFile;
import it.bologna.ausl.riversamento.sender.RiVersatore;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.XPathContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONValue;

/**
 *
 * @author spritz
 */
public class SendToParerWorker implements Chef {

//    private static final String configFile=System.getProperty("ChefWorker.configFile","chefWorker.properties");
    private MongoWrapper mongoWrapper;
    private String uri, username, password, versione, ambiente;
    private static final Logger log = LogManager.getLogger(SendToParerWorker.class);
//    private Properties config;
    private String mongoUri;

    public SendToParerWorker() throws WorkerException {
//        config=Launcher.loadConfig(configFile);
        mongoUri = config.getProperty("mongo.uri", "mongodb://argo:siamofreschi@procton4,procton3/doc?safe=true");

        try {
            Boolean useMinIO = Boolean.parseBoolean(config.getProperty("minio.enabled"));
            JSONObject minIOConfig = (JSONObject) JSONValue.parse(config.getProperty("minio.config"));
            String codiceAzienda = config.getProperty("masterchef.codice-azienda");
            mongoWrapper = MongoWrapper.getWrapper(
                    useMinIO, mongoUri,
                    minIOConfig.get("DBDriver").toString(),
                    minIOConfig.get("DBUrl").toString(),
                    minIOConfig.get("DBUsername").toString(),
                    minIOConfig.get("DBPassword").toString(),
                    codiceAzienda,
                    null);

            ambiente = config.getProperty("parer.ambient", "test");
//            if(ambiente.equalsIgnoreCase("prod")){
//                uri = config.getProperty("sendtoparer.uri", "https://parer.regione.emilia-romagna.it/sacer/VersamentoSync");
//                username = config.getProperty("sendtoparer.username", "gedi_ausl_bo");
//                password = config.getProperty("sendtoparer.password", "la password");
//                versione = config.getProperty("sendtoparer.versionedocumento", "1.0");
//            }
//            else{
//                uri = config.getProperty("sendtoparertest.uri", "https://gedi_ausl_bo_pre:gedi_ausl_bo_pre_pw0@parer-pre.regione.emilia-romagna.it/sacer/VersamentoSync");
//                username = config.getProperty("sendtoparertest.username", "gedi_ausl_bo_pre");
//                password = config.getProperty("sendtoparertest.password", "la password");
//                versione = config.getProperty("sendtoparertest.versionedocumento", "1.4");
//            }
        } catch (Exception e) {
            throw new WorkerException(getJobType(), e);
        }
    }

    @Override
    public JSONObject doWork(JSONObject in, int i, JSONArray otherin, File workDir, File tmpDir) throws WorkerException {
        log.info("parametri doWork - in - \n", in.toJSONString());

        String xmlDocument = (String) in.get("xmlDocument");
        String command = (String) in.get("command");
        JSONArray identityFiles = (JSONArray) in.get("identityFiles");
        //log.info(identityFiles.toJSONString());
        String codiceFiscaleUtenteLoginInternauta = (String) in.get("userloginint");

        Pacco parerDocument = new Pacco();

        JSONObject res = new JSONObject();
        String token;

        // setto i parametri di versamento corretti a seconda dell'ambiente
        setParameters(command);

        parerDocument.setVersione(versione);
        parerDocument.setLoginName(username);
        parerDocument.setPassword(password);

        RiVersatore riversatore = new RiVersatore(uri, username, password, versione);
        boolean dumpCompleted = false;

        SendToParerErrorDetails errorMessage = new SendToParerErrorDetails();

        if (mongoWrapper == null) {
            throw new IllegalArgumentException("Mongo connection is needed for templatePath");
        }

        try {
            // otteniamo il token per eventuale richiesta a internauta
            token = getTokenFromInternauta(codiceFiscaleUtenteLoginInternauta);
            log.info("generazione JWS per richiesta a internauta");
        } catch (Exception ex) {
            token = null;
        }

        //ArrayList<PaccoFile> allegatiAlbo = new ArrayList<PaccoFile>();
        if (identityFiles != null && identityFiles.size() > 0) {

            for (Object identityFile : identityFiles) {
                //IdentityFile identityFile=(IdentityFile)identityFiles.get(j);
                JSONObject jsonObject = (JSONObject) identityFile;
                PaccoFile paccoFile = new PaccoFile();
                // scarica file
                InputStream inputStream = null;
                String uuidMongo = (String) jsonObject.get("uuidMongo");
                String base64 = (String) jsonObject.get("fileBase64");
                if ((uuidMongo != null) && (!uuidMongo.equals("-1"))) {

                    // prova a prendere il file dal repository locale
                    log.info("provo a prendere il file dal repository locale");
                    try {
                        inputStream = mongoWrapper.get(uuidMongo);
                    } catch (MongoWrapperException ex) {
                        log.error("ERRORE mongoWrapper: " + ex);
                    } catch (MongoTimeoutException ex) {
                        log.error("ERRORE MongoTimeoutException: " + ex);
                        log.error("Rilancio Errore sul file " + jsonObject.toString());
                        throw ex;
                    }

                    if (inputStream == null) {
                        log.info("su repository locale non esiste, provo in internauta");
                        try {
                            inputStream = getEmlFromInternauta(token, uuidMongo);
                        } catch (IOException ex) {
                            log.error("errore su funzione getEmlFromInternauta: ", ex);
                            log.error("Errore sul file " + jsonObject.toString());
                        } catch (MongoTimeoutException ex) {
                            log.error("ERRORE MongoTimeoutException: " + ex);
                            log.error("Rilancio Errore sul file " + jsonObject.toString());
                            throw ex;
                        }
                        if (inputStream == null) {
                            log.error("non esiste il file nei repository");
                            log.error("Rilancio Errore sul file " + jsonObject.toString());
                            throw new WorkerException(getJobType(), "File non esistente: " + jsonObject.toString(), errorMessage);
                        }

                    }
                    log.info("inputstream ottenuto");
                } else if (base64 != null) {
                    byte[] fileBytes = Base64Coder.decodeLines(base64);
                    inputStream = new ByteArrayInputStream(fileBytes);
                }
//                 if(identityFile.getUuidMongo() != null){
//                     inputStream = mongoWrapper.getByPath(identityFile.getUuidMongo());
//                 }else if(identityFile.getBase64() != null){
//                     byte[] fileBytes = Base64Coder.decodeLines(identityFile.getFileBase64());
//                     inputStream = new ByteArrayInputStream(fileBytes);
//                 }
                paccoFile.setInputStream(inputStream);
                System.out.println("passaggio");
                String mime = (String) jsonObject.get("mime");
                paccoFile.setMime(mime);
                String idFile = (String) jsonObject.get("id");
                paccoFile.setId(idFile);
//                                 paccoFile.setMime(identityFile.getMime());
                String fileName = (String) jsonObject.get("fileName");
                paccoFile.setFileName(fileName);
                parerDocument.addFile(paccoFile);
            }
        }

        parerDocument.setXmlsip(xmlDocument);

        String result = "";
        try {
            log.info("Invoco riversatore.riversa(parerDocument)...");
            result = riversatore.riversaNuovo(parerDocument);
            log.info("Result: " + (result != null ? result : "is NULL"));
            Builder parser = new Builder();
            log.info("Parso il result");
            Document resd = parser.build(result, null);
            log.info("Documento resd creato");
            // parsare con XOM l'esito
            log.info("Creo un XPathContext");
            XPathContext context = new XPathContext("EsitoVersamento", "");
            log.info("Setto il context nel documento...");
            Nodes node = resd.query("//CodiceEsito/text()", context);

            if (node.get(0).toXML().equals("POSITIVO") || node.get(0).toXML().equals("WARNING")) {
                dumpCompleted = true;
                res.put("parerResponse", result);
                log.info("response finalizzata: " + res.toString());
            } else {
                log.error("c'e' un problema nel context...");
                log.error("Creo il nodo errorText...");
                Nodes errorText = resd.query("//MessaggioErrore/text()", context);
                log.error("Creo il nodo codErrore...");
                Nodes codErrore = resd.query("//CodiceErrore/text()", context);

                log.error("compilo l'erroMessage");
                errorMessage.setParerResponse(result);
                String errorCode = codErrore.get(0).toXML();
                errorMessage.setErrorCode(codErrore.get(0).toXML());
                String utf8ErrorMessage = toUTF8(errorText.get(0).toXML());
                errorMessage.setErrorMessage(utf8ErrorMessage);
                errorMessage.setErrorType("NEGATIVO");
                if (errorCode != null && utf8ErrorMessage != null) {
                    log.error("Codice Errore " + errorCode + ": " + utf8ErrorMessage);
                }
                log.error("errorMessage compilato");
            }

        } catch (Throwable ex) {
            log.error("errore", ex);
            if (result == null) {
                log.error("result e' null, quindi creo un errore con codice SERVIZIO");
                errorMessage.setErrorCode("SERVIZIO");
                errorMessage.setErrorMessage("Errore nel server. Non è stato possibile elaborare la richiesta");
                errorMessage.setErrorType("NEGATIVO");
                errorMessage.setParerResponse(null);
            } else {
                log.error("result e' popolato, quindi creo un errore con codice INTERNAL");
                errorMessage.setErrorCode("INTERNAL");
                errorMessage.setErrorMessage(ex.toString());
                errorMessage.setErrorType("NEGATIVO");
                errorMessage.setParerResponse(result);
            }
            log.error("Rilancio WorkerException");
            throw new WorkerException(getJobType(), "Errore su riversamento", errorMessage);
        }

        /*
        PaccoFile paccoFile = new PaccoFile();
        paccoFile.setFile(new File(path));
        paccoFile.setMime("application/pdf");
        paccoFile.setFileName(paccoFile.getFile().getName());
        xmlGenerator.addAllegato(db.getDoc(), paccoFile);
        pacco.addFile(paccoFile);
         */
        if (dumpCompleted) {
            return res;
        } else {
            throw new WorkerException(getJobType(), "Errore su riversamento", errorMessage);
        }
    }

    private String toUTF8(String isoString) throws UnsupportedEncodingException {
        String out = null;
        out = new String(isoString.getBytes("UTF-8"), "UTF-8");
//        String utf8String = isoString;
//        if (isoString!= null && !isoString.equals("")) {
//            try {
//                byte[] stringBytesISO = isoString.getBytes("ISO-8859-1");
//                utf8String = new String(stringBytesISO, "UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                // As we can't translate just send back the best guess.
//                System.out.println("UnsupportedEncodingException is: " + e.getMessage());
//                utf8String = isoString;
//            }
//        }
        return out;
    }

    private void setParameters(String command) throws WorkerException {
        try {
            if (command.equalsIgnoreCase("insert")) {
                if (ambiente.equalsIgnoreCase("prod")) {
                    uri = config.getProperty("sendtoparer.insert.uri", "https://parer.regione.emilia-romagna.it/sacer/VersamentoSync");
                    username = config.getProperty("sendtoparer.insert.username", "gedi_ausl_bo");
                    password = config.getProperty("sendtoparer.insert.password", "la password");
                    versione = config.getProperty("sendtoparer.insert.versionedocumento", "1.0");
                } else {
                    uri = config.getProperty("sendtoparer.test.insert.uri", "https://gedi_ausl_bo_pre:gedi_ausl_bo_pre_pw0@parer-pre.regione.emilia-romagna.it/sacer/VersamentoSync");
                    username = config.getProperty("sendtoparer.test.insert.username", "gedi_ausl_bo_pre");
                    password = config.getProperty("sendtoparer.test.insert.password", "la password");
                    versione = config.getProperty("sendtoparer.test.insert.versionedocumento", "1.4");
                }
            } else if (command.equalsIgnoreCase("update")) {
                if (ambiente.equalsIgnoreCase("prod")) {
                    uri = config.getProperty("sendtoparer.update.uri", "https://parer.regione.emilia-romagna.it/sacer/AggiuntaAllegatiSync");
                    username = config.getProperty("sendtoparer.update.username", "gedi_ausl_bo");
                    password = config.getProperty("sendtoparer.update.password", "la password");
                    versione = config.getProperty("sendtoparer.update.versionedocumento", "1.0");
                } else {
                    uri = config.getProperty("sendtoparer.test.update.uri", "https://gedi_ausl_bo_pre:gedi_ausl_bo_pre_pw0@parer-pre.regione.emilia-romagna.it/sacer/AggiuntaAllegatiSync");
                    username = config.getProperty("sendtoparer.test.update.username", "gedi_ausl_bo_pre");
                    password = config.getProperty("sendtoparer.test.update.password", "la password");
                    versione = config.getProperty("sendtoparer.test.update.versionedocumento", "1.4");
                }
            } else {
                throw new WorkerException(getJobType(), "command: " + command);
            }
        } catch (Exception e) {
            throw new WorkerException(getJobType(), e);
        }
    }

    public InputStream getEmlFromInternauta(String token, String uuid) throws IOException {

        String downloadUrl = config.getProperty("internauta.download.eml.uri", "https://gdml.internal.ausl.bologna.it/internauta-api/resources/shpeck/downloadEml");

        HashMap headers = new HashMap();
        headers.put("Authorization", "Bearer " + token);
        headers.put("Content-Type", "application/json");
        headers.put("application", "gedi");

        String url = downloadUrl + "?uuidRepository=" + uuid;

        HttpCallResponse response = httpCallWithHeaders(url, headers, "application/json", null, 600, "GET");

        if (response != null) {
            return response.getByteStream();
        }

        return null;
    }

    @Override
    public String getJobType() {
        return "SendToParer";
    }

}
