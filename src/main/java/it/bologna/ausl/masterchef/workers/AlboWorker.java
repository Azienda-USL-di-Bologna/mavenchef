package it.bologna.ausl.masterchef.workers;

import it.bologna.ausl.masterchef.Chef;
import static it.bologna.ausl.masterchef.Launcher.config;
import it.bologna.ausl.masterchef.errors.WorkerException;
import it.bologna.ausl.mongowrapper.MongoWrapper;
import it.bologna.ausl.mongowrapper.exceptions.MongoWrapperException;
import it.bologna.ausl.utils.albo.AlboAllegato;
import it.bologna.ausl.utils.albo.AlboDoc;
import it.bologna.ausl.utils.albo.AlboManager;
import it.bologna.ausl.utils.albo.AlboMod;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/*
 "params": {
 "idDoc": "idDoc",
 "tipoDocumento": "tipoDocumento",
 "strutturaPubblicante": "strutturaPubblicante",
 "articolazione": "articolazione",
 "oggettoPub": "oggettoPub",
 "numRegistro": "numero",
 "registro": "registro",
 "numero": "numero",
 "dataDoc": "dataDoc",
 "dataEsecutivita": "dataEsecutivita",
 "dataAdozione": "dataAdozione",
 "validita": "validita",
 "dal": "dal",
 "al": "al",
 "anno": "anno",
 "tipoProvvedimento":"tipoProvvedimento",
 "contenuto":"contenuto",
 "eventualeSpesaPrevista":"eventualeSpesaPrevista",
 "estremiDocumentiRelativi":"estremiDocumentiRelativi",
 "nomeMongoRelataTemplate": "REGNUM_ANNO_Relata_[id_pubblicazione].pdf"
 "allegati": [
 "uidAllegato1",
 "uidAllegato2",
 ...
 ],
 "ops": [ {
 "op": "INTEGRA/ANNULLA",
 "ldoc": "idDoc da integrare/annullare"
 },
 ...
 ]
 }

 "res": {
 "status": "success|error"
 "url_relata": "url_relata",
 "id_pubblicazione": "id_pubblicazione",
 "uuidRelata": "uuidRelata",
 "dal": "dal",
 "al": "al"
 }

 */
public class AlboWorker implements Chef {

//    private static final String configFile = System.getProperty("ChefWorker.configFile", "chefWorker.properties");
    private final String postUri, username, password;
    private MongoWrapper m;
    private File[] tempFiles;

    public AlboWorker() throws WorkerException {
//        Properties config = Launcher.loadConfig(configFile);
        String mongoUri = config.getProperty("mongo.uri", "mongodb://argo:siamofreschi@procton4,procton3/doc?safe=true");
        try {
            Boolean useMinIO = Boolean.parseBoolean(config.getProperty("minio.enabled"));
            JSONObject minIOConfig = (JSONObject) JSONValue.parse(config.getProperty("minio.config"));
            String codiceAzienda = config.getProperty("masterchef.codice-azienda");
            m = MongoWrapper.getWrapper(
                    useMinIO, mongoUri, 
                    minIOConfig.get("DBDriver").toString(), 
                    minIOConfig.get("DBUrl").toString(), 
                    minIOConfig.get("DBUsername").toString(), 
                    minIOConfig.get("DBPassword").toString(), 
                    codiceAzienda,
                    null);
            //m = new MongoWrapper(mongoUri);
        } catch (Exception e) {
            throw new WorkerException(getJobType(), e);
        }
        postUri = config.getProperty("albo.uri", "http://vm-zope-dev2:8380/eauslbo/albo_pubblica_doc");
        username = config.getProperty("albo.username", "utente");
        password = config.getProperty("albo.password", "utente");
    }

    @Override
    public JSONObject doWork(JSONObject in, int i, JSONArray otherin, File workDir, File tmpDir) throws WorkerException {
        //estrazione dati scalari dal json
        String dal = (String) in.get("dal");
        String al = (String) in.get("al");
        String nomeMongoRelataTemplate = (String) in.get("nomeMongoRelataTemplate");

        AlboDoc adoc = new AlboDoc();
        adoc.setDal(dal);
        adoc.setAl(al);
        adoc.setAnno((String) in.get("anno"));
        adoc.setDataAdozione((String) in.get("dataAdozione"));
        adoc.setDataDoc((String) in.get("dataDoc"));
        adoc.setDataEsecutivita((String) in.get("dataEsecutivita"));
        adoc.setIdDoc((String) in.get("idDoc"));
        adoc.setNumero((String) in.get("numero"));
        adoc.setOggettoPub((String) in.get("oggettoPub"));
        adoc.setRegistro((String) in.get("registro"));
        adoc.setNumRegistro((String) in.get("numRegistro"));
        adoc.setArticolazione((String) in.get("articolazione"));
        adoc.setStrutturaPubblicante((String) in.get("strutturaPubblicante"));
        adoc.setTipoDocumento((String) in.get("tipoDocumento"));
        adoc.setValidita((String) in.get("validita"));
        //TRASPSARENZA
        adoc.setTipoProvvedimento((String) in.get("tipoProvvedimento"));
        adoc.setContenuto((String) in.get("contenuto"));
        adoc.setEventualeSpesaPrevista((String) in.get("eventualeSpesaPrevista"));
        adoc.setEstremiDocumentiRelativi((String) in.get("estremiDocumentiRelativi"));

        //estrazione allegati
        JSONArray allegati = (JSONArray) in.get("allegati");
        ArrayList<AlboAllegato> allegatiAlbo = new ArrayList<AlboAllegato>();
        if (allegati != null && allegati.size() > 0) {
            tempFiles = new File[allegati.size()];
            for (int j = 0; j < allegati.size(); j++) {
                InputStream ais;
                String aname;
                String attachUuid = (String) allegati.get(j);
                try {
                    ais = m.get(attachUuid);
                    aname = m.getFileName(attachUuid);
                } catch (MongoWrapperException e) {
                    throw new WorkerException(getJobType(), "errore nel download del file allegato", e);
                }
                File allegatoFile = null;
                try {
                    allegatoFile = new File(workDir.getAbsolutePath() + "/" + getJobType() + "_" + attachUuid + "_" + aname);
                    saveFile(ais, allegatoFile);
                    tempFiles[j] = allegatoFile;
                } catch (Exception ex) {
                    throw new WorkerException(getJobType(), "errore nella creazione del file dell'allegato", ex);
                }
                allegatiAlbo.add(new AlboAllegato(aname, allegatoFile));
            }
        }
        adoc.setAllegati(allegatiAlbo);

        //estrazione modifiche
        //"mods":[{"ops":"annulla","ldoc":["1234","456"]}]
        ArrayList<AlboMod> mods = new ArrayList<AlboMod>();
        JSONArray jmods = (JSONArray) in.get("ops");
        if (jmods != null && jmods.size() > 0) {
            for (int j = 0; j < jmods.size(); j++) {
                JSONObject jmod = (JSONObject) jmods.get(j);
                String op = (String) jmod.get("op");
                ArrayList<String> docs = (ArrayList<String>) jmod.get("ldoc");
                AlboMod amod = new AlboMod();
                if (op != null && op.equals("annulla")) {
                    amod.setOperation(AlboMod.Operation.ANNULLA);
                } else if (op != null && op.equals("integra")) {
                    amod.setOperation(AlboMod.Operation.INTEGRA);
                } else {
                    throw new WorkerException(getJobType(), "Invalid op specified");
                }
                amod.setDoclist(docs);
                mods.add(amod);
            }
            adoc.setMods(mods);
        }

        AlboManager am;
        try {
            am = new AlboManager(postUri, username, password);
        } catch (Exception ex) {
            throw new WorkerException(getJobType(), ex);
        }
        try {
            String res = am.pubDocMulipart(adoc);
            JSONObject jres = (JSONObject) JSONValue.parse(res);
            String status = null;
            if (jres != null) {
                status = (String) jres.get("status");;
            }
            if (status != null && status.equalsIgnoreCase("success")) {
                String idPubblicazione = (String) jres.get("id_pubblicazione");
                String urlRelata = (String) jres.get("url_relata");
                InputStream relataIs = fetchRelata(urlRelata);
//                String mongoFileName = adoc.getTipoDocumento() + "-" + adoc.getRegistro() + adoc.getNumero() + "-" + adoc.getAnno() + "-" + idPubblicazione + ".pdf";
                String nomeRelata = nomeMongoRelataTemplate;
                String mongoFileName;
                if (nomeRelata == null || nomeRelata.equals("")) {
                    mongoFileName = adoc.getTipoDocumento() + "-" + adoc.getRegistro() + adoc.getNumero() + "-" + adoc.getAnno() + "-" + idPubblicazione + ".pdf";
                } else {
                    mongoFileName = nomeRelata.replace("[id_pubblicazione]", idPubblicazione);
                }
                String uuidRelata = m.put(relataIs, mongoFileName, "/Relate", true);
                jres.put("uuidRelata", uuidRelata);
                jres.put("dal", dal);
                jres.put("al", al);
            } else {
                throw new WorkerException(getJobType(), res);
            }
            return jres;

        } catch (Exception ex) {
            throw new WorkerException(getJobType(), ex);
        } finally {
            if (tempFiles != null && tempFiles.length > 0) {
                try {
                    for (int j = 0; j < tempFiles.length; j++) {
                        if (tempFiles[j] != null) {
                            tempFiles[j].delete();
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

    private void saveFile(InputStream is, File fileToCreate) throws FileNotFoundException, IOException {
        OutputStream os = new FileOutputStream(fileToCreate);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
    }

    private static InputStream fetchRelata(String url) throws ClientProtocolException, IOException {
        HttpClient c = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        c.getConnectionManager().getSchemeRegistry().register(
                new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
        );
        HttpResponse res = c.execute(get);
        return res.getEntity().getContent();
    }

    @Override
    public final String getJobType() {
        return "Albo";
    }

}
