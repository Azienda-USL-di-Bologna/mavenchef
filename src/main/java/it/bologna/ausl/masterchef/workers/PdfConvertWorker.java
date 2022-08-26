package it.bologna.ausl.masterchef.workers;

import com.itextpdf.text.DocumentException;
import it.bologna.ausl.masterchef.Chef;
import it.bologna.ausl.masterchef.utils.Base64Coder;
import it.bologna.ausl.masterchef.errors.PdfConvertErrorDetails;
import it.bologna.ausl.masterchef.errors.WorkerException;
import it.bologna.ausl.masterchef.utils.NullUserAgent;
import it.bologna.ausl.masterchef.utils.UtilityFunctions;
import it.bologna.ausl.mongowrapper.MongoWrapper;
import it.bologna.ausl.mongowrapper.exceptions.MongoWrapperException;
import it.bologna.ausl.pdfconverterandsigner.PdfConvertClient;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.client.HttpResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypeException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;
import static it.bologna.ausl.masterchef.Launcher.config;
import static it.bologna.ausl.masterchef.Launcher.connectAndGetRegex;
import static it.bologna.ausl.masterchef.Launcher.emlReplaceMap;
import it.bologna.ausl.mimetypeutilities.Detector;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;


/*
 INPUT
pdfconvertjob={
    'appID':"rediscli",
    'jobID':"1",
    'jobList':[ {
        'jobN':"job1",
        'jobType':"PdfConvert",
        'params': {
            'file': "base64File"
            'srcFileUuid': "sourceFileSuMongo"
            'convertedFilePath': "convertedFilePathSuMongo"
            'noErrorIfNotFound': "true|false"
        }
    }],
    'returnQueue':"queueName"
}
    * Cose da ricordare:
    * se specificato 'file', il parametro 'srcFileUuid' è ignorato, altrimenti il file è preso da 'srcFileUuid' su mongo
    * se specificato 'convertedFilePath' il file convertito viene anche salvato su mongo nel percorso indicato

OUTPUT
{
    'pdf': "uuid in mongodownload"
    'uuidConvertedFile' : "uuidConvertedFile"
}
    * Cose da ricordare:
    * ritorna sempre il file convertito in base64
    * se è stato passato il parametro 'convertedFilePath' torna l'uuid su mongo del file convertito
 */
public class PdfConvertWorker implements Chef {
//private static final String configFile=System.getProperty("ChefWorker.configFile","chefWorker.properties");

    public static final int UNSUPPORTED_MEDIA_TYPE = 415;

    private static final Logger log = LogManager.getLogger(PdfConvertWorker.class);

    private String pdfConverterUrl;
    private PdfConvertClient pdfc;
    private MongoWrapper m, md;

    public PdfConvertWorker() throws WorkerException {
//        Properties config = Launcher.loadConfig(configFile);
        pdfConverterUrl = config.getProperty("pdfconvert.url", "http://procton2:9080/firmasemplice/PdfConvert");
//        pdfConverterUrl = config.getProperty("pdfconvert.url","https://pico.internal.ausl.bologna.it/firmasemplice/PdfConvert");
//        pdfConverterUrl = config.getProperty("pdfconvert.url","http://localhost:8082/firmasemplice/PdfConvert");
        pdfc = new PdfConvertClient(pdfConverterUrl, null, null);
        String mongoUri = config.getProperty("mongo.uri", "mongodb://argo:siamofreschi@procton4,procton3/doc?safe=true");
        String mongoDownloadUri = config.getProperty("mongodownload.uri", "mongodb://argo:siamofreschi@procton3/downloadgdml");
        try {
            Boolean useMinIO = Boolean.parseBoolean(config.getProperty("minio.enabled"));
            JSONObject minIOConfig = (JSONObject) JSONValue.parse(config.getProperty("minio.config"));
            String codiceAzienda = config.getProperty("masterchef.codice-azienda");
            Integer maxPoolSize = Math.toIntExact((Long) minIOConfig.get("maxPoolSize"));
            m = MongoWrapper.getWrapper(
                    useMinIO, mongoUri,
                    minIOConfig.get("DBDriver").toString(),
                    minIOConfig.get("DBUrl").toString(),
                    minIOConfig.get("DBUsername").toString(),
                    minIOConfig.get("DBPassword").toString(),
                    codiceAzienda,
                    maxPoolSize,
                    null);
            md = MongoWrapper.getWrapper(
                    useMinIO, mongoDownloadUri,
                    minIOConfig.get("DBDriver").toString(),
                    minIOConfig.get("DBUrl").toString(),
                    minIOConfig.get("DBUsername").toString(),
                    minIOConfig.get("DBPassword").toString(),
                    codiceAzienda + "t",
                    maxPoolSize,
                    null);
        } catch (Exception e) {
            throw new WorkerException(getJobType(), e);
        }
    }

    @Override
    public JSONObject doWork(JSONObject in, int j, JSONArray otherin, File workDir, File tmpDir) throws WorkerException {
        File srcTempFile = null;
        try {

            boolean fromMongo;
            String base64File = (String) in.get("file");
            String srcFileUuid = (String) in.get("srcFileUuid");
            String convertedFilePath = (String) in.get("convertedFilePath");
            String srcFileName = null;
            if (convertedFilePath != null && !convertedFilePath.equals("")) {
                convertedFilePath = MongoWrapper.getDname(convertedFilePath).replace("\\", "/") + "/" + MongoWrapper.getFname(convertedFilePath);
            }
            boolean noErrorIfNotFound = false;
            try {
                noErrorIfNotFound = ((Boolean) in.get("noErrorIfNotFound"));
            } catch (Exception ex) {
            }
            JSONObject res = new JSONObject();

            InputStream srcInputStream = null;
            if (base64File != null && !base64File.equals("")) {
                fromMongo = false;
                byte[] fileBytes = Base64Coder.decodeLines(base64File);
                srcInputStream = new ByteArrayInputStream(fileBytes);
            } else {
                fromMongo = true;
                srcInputStream = m.get(srcFileUuid);
                if (srcInputStream == null && noErrorIfNotFound) {
                    return res;
                } else {
                    srcFileName = m.getFileName(srcFileUuid);
                    if (srcFileName.equals(MongoWrapper.getFname(convertedFilePath))) {
                        if (noErrorIfNotFound) {
                            log.warn("Il nome del file pdf da creare è uguale al nome del pdf da convertire");
                            return res;
                        } else {
                            throw new WorkerException(getJobType(), "Il nome del file pdf da creare è uguale al nome del pdf da convertire");
                        }
                    }
                }
            }
            String ext = null;
            if (fromMongo) {
                log.info("src file from mongo, filename: " + srcFileName);
                ext = UtilityFunctions.getExtensionFromFileName(srcFileName);
            }

            boolean isHtml;
            InputStream toMongoIs = null;
            try {
                srcTempFile = File.createTempFile(getClass().getSimpleName() + "_", "." + ext);
                UtilityFunctions.inputStreamToFile(srcInputStream, srcTempFile);
                isHtml = isHtml(srcTempFile);
            } catch (Exception ex) {
                throw new WorkerException(getJobType(), ex.getMessage());
            }
            if (isHtml) {
                try {
                    log.info("html detected, using ITextRenderer conversion");
                    PdfConvertWorker.parseHtml(srcTempFile);
                    byte[] pdfBytes = PdfConvertWorker.htmlToPdf(srcTempFile, "UTF-8");
                    toMongoIs = new ByteArrayInputStream(pdfBytes);
//                InputStream htmlIs = srcInputStream;
//                PipedInputStream pin = new PipedInputStream();
//                final PipedOutputStream pout = new PipedOutputStream(pin);
//                new Thread(
//                  new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            htmlToPdf(htmlIs, pout);
//                        }
//                        catch (Exception ex) {
//                            throw new WorkerException(getJobType(), ex.getMessage());
//                        }
//                    }
//                }).start();
//            }
//            catch (Exception ex) {
//                ex.printStackTrace();
//            }
                } catch (Exception ex) {
                    log.error("errore nella conversione dell'html in pdf", ex);
                    throw new WorkerException(getJobType(), "Errore del server", new PdfConvertErrorDetails(UNSUPPORTED_MEDIA_TYPE, ex.getMessage()));
                }
            } else {
                byte[] convertedFileBytes;
                try (FileInputStream is = new FileInputStream(srcTempFile)) {
                    convertedFileBytes = pdfc.convert(is, true, null);
                } catch (HttpResponseException ex) {
                    throw new WorkerException(getJobType(), "Errore del server", new PdfConvertErrorDetails(ex.getStatusCode(), ex.getMessage()));
                } catch (Exception ex) {
                    throw new WorkerException(getJobType(), ex);
                }
                toMongoIs = new ByteArrayInputStream(convertedFileBytes);
            }

            String outPdfUuid = null;
            try {
                if (convertedFilePath != null && !convertedFilePath.equals("")) {
                    if (srcFileUuid != null && !srcFileUuid.equals("")) {
                        m.deleteByPath(convertedFilePath);
                        if (m.existsObjectbyUUID(srcFileUuid)) {
                            outPdfUuid = m.put(toMongoIs, MongoWrapper.getFname(convertedFilePath), MongoWrapper.getDname(convertedFilePath), true);
                            res.put("uuidConvertedFile", outPdfUuid);
                        } else {
                            if (noErrorIfNotFound) {
                                return res;
                            } else {
                                throw new WorkerException(getJobType(), "Il file su Mongo non esiste più");
                            }
                        }
                    }
                }
                res.put("sizeConvertedFile", m.getSizeByUuid(outPdfUuid));
                String convertedUuidMongoDownload = md.put(m.get(outPdfUuid), "pdf_convert.pdf", "/temp", false);
                //res.put("pdf", Base64Coder.encodeLines(convertedFileBytes));
                res.put("pdf", convertedUuidMongoDownload);

            } catch (Exception ex) {
                throw new WorkerException(getJobType(), ex);
            }
            return res;
        } catch (MongoWrapperException ex) {
            throw new WorkerException(getJobType(), ex);
        } finally {
            if (srcTempFile != null && srcTempFile.exists()) {
                srcTempFile.delete();
            }
        }
    }

    @Override
    public final String getJobType() {
        return "PdfConvert";
    }

    private static byte[] test(byte[] bytes) throws IOException {
        String text = new String(bytes, "UTF-8");
        byte[] recoded = text.getBytes("Cp1252");
        return recoded;
    }
    
    private static byte[] htmlToPdf(File htmlIn, String encoding) throws DocumentException, IOException {
        //from html to xhtml
        try (FileInputStream is = new FileInputStream(htmlIn)) {
            Tidy tidy = new Tidy();
            tidy.setShowWarnings(false);
            tidy.setXmlTags(false);
            tidy.setInputEncoding(encoding);
            tidy.setOutputEncoding("UTF-8");
            tidy.setXHTML(true);//
            tidy.setMakeClean(true);
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                tidy.parseDOM(is, bos);
//            tidy.pprint(xmlDoc, new FileOutputStream("c:/a.xhtml"));
                // setto uno userAgent personalizzato che non esegue il download delle risorse (per evitare che l'applicazione esca su iternet e venga bloccata dal proxy)
                NullUserAgent userAgent = new NullUserAgent(ITextRenderer.DEFAULT_DOTS_PER_POINT);
                ITextRenderer renderer = new ITextRenderer(ITextRenderer.DEFAULT_DOTS_PER_POINT, ITextRenderer.DEFAULT_DOTS_PER_PIXEL, userAgent);
                try {
                    renderer.setDocument(bos.toByteArray());
                } catch (Exception ex) {
                    if (!encoding.equals("ANSI"))
                        return htmlToPdf(htmlIn, "ANSI");
                    else {
                        throw ex;
                    }
                }
//                ITextUserAgent userAgent = new ITextUserAgent(renderer.getOutputDevice());
//                renderer.getSharedContext().setUserAgentCallback(userAgent);
                renderer.layout();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                renderer.createPDF(byteArrayOutputStream, true);
                return byteArrayOutputStream.toByteArray();
            }
            
        }
    }
//    

    public static void main(String[] args) throws DocumentException, IOException, WorkerException, UnsupportedEncodingException, MimeTypeException {
        emlReplaceMap = connectAndGetRegex();
        File file = new File("F:\\Progetti\\ProgettiJava\\testAllegati\\allegati\\2\\6_testo.html");
        System.out.println(isHtml(file));
        byte[] convertedFileBytes;
//                try (FileInputStream is = new FileInputStream(file)) {
//                    PdfConvertWorker pdfConvert = new PdfConvertWorker();
//                    convertedFileBytes = pdfConvert.pdfc.convert(is, true, null);
//                }
        parseHtml(file);
        convertedFileBytes = htmlToPdf(file, "UTF-8");

        try (FileOutputStream fos = new FileOutputStream("D:/prova.pdf")) {
            IOUtils.write(convertedFileBytes, fos);
        }

    }

    

    private static boolean isHtml(File f) throws IOException, UnsupportedEncodingException, MimeTypeException {
        Detector d = new Detector();
        String mimeType = d.getMimeType(f.getAbsolutePath());
        return mimeType.equalsIgnoreCase(MediaType.TEXT_HTML.toString());
    }

//    Purifica il file da eventuali tag o css che rompono il pdfconverter
//    Per fare questo convertiamo il file in stringa in modo da poter applicare la regular expression che purifica il file e poi lo riconvertiamo nuovamente in file
    private static void parseHtml(File f) throws IOException {

        String str = FileUtils.readFileToString(f, "UTF-8");

        InputStream inputStream = null;
        try (FileOutputStream os = new FileOutputStream(f)) {

            //mi connetto al db e prendo le regex e le eventuali sostituzioni
           // Map<String, String> connectAndGetRegex = connectAndGetRegex();
            for (Map.Entry<String, String> row : emlReplaceMap.entrySet()) {
                String regex = row.getKey();
                String replacement = row.getValue();
                if(replacement==null) replacement="";
                str = str.replaceAll(regex, replacement);
                log.info("sostituisco " + regex + " con " + replacement);
            }
            str = str.replaceAll("=" + System.getProperty("line.separator"), "").replaceAll(">", "> ").replaceAll("<+(\\S+@\\S+)>", "$1");
//            str = str.replaceAll("@page\\s*WordSection1[\\s\\S]*?(?=\\{)\\{[\\s\\S]*?(?=\\})\\}", "");
//            str = str.replaceAll("@page\\s*Section1[\\s\\S]*?(?=\\{)\\{[\\s\\S]*?(?=\\})\\}", "");
//            str = str.replaceAll("div.WordSection1[\\s\\S]*?(?=\\{)\\{[\\s\\S]*?(?=\\})\\}", "");
//            str = str.replaceAll("div.Section1[\\s\\S]*?(?=\\{)\\{[\\s\\S]*?(?=\\})\\}", "");
////            str = str.replaceAll("src=[3D]{0,2}\\\"data:image\\/([a-zA-Z]*);base64,([^\\\"]*)\\\"", "");
//            str = str.replaceAll("<o\\:.*?>", "<p style=\"padding:0;margin:0\">");
//            str = str.replaceAll("<\\/o\\:.*?>", "</p>");
//            str = str.replaceAll("!\\[.*?\\]", "");
//            str = str.replaceAll("=(3D)('|\"|\\s'|\\s\")", "=$2");
//            str = str.replaceAll("<!--[\\s\\S]*?-->", "");

            //tolgo roba microsoft che rompe le palle
//            str = str.replaceAll("class=WordSection1", "");
//            str = str.replaceAll("class=\"MsoNormalTable\"|class=MsoNormalTable", "");
//            str = str.replaceAll("class=MsoNormal|class=\"MsoNormal\"", "");
//            str = str.replaceAll("class=MsoListParagraph|class=\"MsoListParagraph\"", "");
//            str = str.replaceAll(";mso-fareast-language:IT", "");
//            str = str.replaceAll("mso-fareast-language:IT", "");
//            str = str.replaceAll(";mso-list:l0 level1 lfo1", "");
//            str = str.replaceAll("mso-list:l0 level1 lfo1", "");

            //tolgo gli style vuoti
//            str = str.replaceAll("style=\"\\s.*?\"", "");
//            str = str.replaceAll("style='\\s.*?'", "");
//            str = str.replaceAll("style=''", "");

            byte[] fileBytes = str.getBytes();
            inputStream = new ByteArrayInputStream(fileBytes);

            //Lo leggiamo a blocchi di 1024
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
