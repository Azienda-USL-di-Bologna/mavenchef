package it.bologna.ausl.masterchef.workers;


import com.itextpdf.text.pdf.ICC_Profile;
import com.itextpdf.text.pdf.PdfAConformanceLevel;
import com.itextpdf.text.pdf.PdfAWriter;
import com.itextpdf.text.pdf.PdfBoolean;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfWriter;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import it.bologna.ausl.masterchef.Chef;
import static it.bologna.ausl.masterchef.Launcher.config;
import it.bologna.ausl.masterchef.errors.WorkerException;
import it.bologna.ausl.masterchef.utils.QRgenerator;
import it.bologna.ausl.mongowrapper.MongoWrapper;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.PDFCreationListener;



/**
INPUT {
    templatePath:"/path/su/mongo"
    'data': {
        'fieldName': fieldValue
    }
    'qrCodeData': {
        'fieldName' : qrCodeFieldName
        'value' : "Azienda USL di Bologna Codice AOO:ASL_BO:protocollo generale - [numero] [data] [ora]"
    }
}
OUTPUT {
    pdf:String (uuid in mongodownload)
}
 */


public class ReporterWorker implements Chef{

//    private static final String configFile=System.getProperty("ChefWorker.configFile","chefWorker.properties");
    private MongoWrapper m, md;
    private static final Logger log = LogManager.getLogger(ReporterWorker.class);
    private final String resourcePathRelative;
    private InputStream iccProfileStream;
    private static final String TEMPLATES_PATH = "templates";
     
    public ReporterWorker() throws WorkerException{
//        Properties config=Launcher.loadConfig(configFile);
        String mongoUri=config.getProperty("mongo.uri","mongodb://argo:siamofreschi@procton4,procton3/doc?safe=true");
        String mongoDownloadUri=config.getProperty("mongodownload.uri","mongodb://argo:siamofreschi@procton3/downloadgdml");
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
            md = MongoWrapper.getWrapper(
                useMinIO, mongoDownloadUri, 
                minIOConfig.get("DBDriver").toString(), 
                minIOConfig.get("DBUrl").toString(), 
                minIOConfig.get("DBUsername").toString(), 
                minIOConfig.get("DBPassword").toString(), 
                codiceAzienda + "t",
                null);
        }
        catch (Exception ex) {
            throw new WorkerException(getJobType(), ex);
        }
//        iccProfileStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/it/bologna/ausl/masterchef/resource/AdobeRGB1998.icc");
        resourcePathRelative = config.getProperty("reporter.resource","reporter");
        org.apache.log4j.Logger.getLogger("org.apache").setLevel(org.apache.log4j.Level.ERROR);
    }
    
    @Override
    public JSONObject doWork(JSONObject in, int i, JSONArray otherin, File workDir, File tmpDir) throws WorkerException {

        ByteArrayOutputStream pdfOut = null;
        ByteArrayOutputStream baos = null;
        FileOutputStream qrCodeOutputStream = null;
        File qrcodeTmpFile = null;

        iccProfileStream = getClass().getResourceAsStream("/it/bologna/ausl/mavenchef/AdobeRGB1998.icc");
        if(iccProfileStream==null){
        log.info("iccprofile è null cazoooo");
            
        }
        Template temp = null;
        JSONObject data = null;
        try {
            String templateName = (String)in.get("templateName");

            // TODO: lasciato per retrocompatibilità, è da togliere quando finiamo il refactoring dei template, il parametro giusto è templateName
            String templatePath = (String)in.get("templatePath");

            log.info("templateName: " + templateName);
            log.info("TemplatePath: " + templatePath);

            if ((templateName == null || templateName.equals("")) && (templatePath == null || templatePath.equals(""))) {
                throw new WorkerException(getJobType(), "templateName o templatePath mancante");
            }

            data = (JSONObject)in.get("data");
            if (data == null){
                throw new WorkerException(getJobType(), "data mancante");
            }

            // generazione e inserimento del QrCode
//            String titleQrcode = "Azienda USL di Bologna Codice AOO:ASL_BO:protocollo generale- numero data ora";
            JSONObject qrCodeData = null;
            // il QrCode non è obbligatorio; se non viene passato ottengo un NullPointerException per cui lo ignoro
            try {
                qrCodeData = (JSONObject) in.get("qrCodeData");
            }
            catch (NullPointerException ex) {
            }
            if (qrCodeData != null) {
                String qrCodeFieldName = (String) qrCodeData.get("fieldName");
                if (qrCodeFieldName == null || qrCodeFieldName.equals("")){
                    throw new WorkerException(getJobType(), "campo fieldName del QrCode mancante");
                }
                String qrCodeValue = (String) qrCodeData.get("value");
                if (qrCodeValue == null || qrCodeValue.equals("")){
                    throw new WorkerException(getJobType(), "campo value del QrCode mancante");
                }
                BufferedImage bi = QRgenerator.generate(qrCodeValue, 200, 200);
                qrcodeTmpFile = File.createTempFile("reporter_qrcode_", null, tmpDir);
                qrCodeOutputStream = new FileOutputStream(qrcodeTmpFile);
                if (!ImageIO.write(bi, "png", qrCodeOutputStream)) {
                    throw new WorkerException(getJobType(), "Errore nella creazione del qrCodeFile");
                }
                qrCodeOutputStream.flush();
                data.put(qrCodeFieldName, qrcodeTmpFile.getAbsolutePath()); 
            }
            File resourcePath = new File(workDir, resourcePathRelative);

            Configuration cfg = new Configuration();
            log.info("resourcePath: " + resourcePath);
            cfg.setDirectoryForTemplateLoading(resourcePath);
            cfg.setObjectWrapper(new DefaultObjectWrapper());
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
            cfg.setIncompatibleImprovements(new Version(2, 3, 20));
            
            data.put("resourcePath", resourcePath.getAbsolutePath().replace("\\", "/"));
//            Set keySet = data.keySet();
//            for (Object key: keySet) {
//                data.put(key.toString(), HtmlEncoder.encode((String)data.get(key)));
//            }

            String templateString;
            if (templateName != null && !templateName.equals("")) {
                log.info("uso templateName");
                Path templateFilePath = Paths.get(resourcePath.getAbsolutePath(), TEMPLATES_PATH, templateName);
                templateString = IOUtils.toString(templateFilePath.toUri(), StandardCharsets.UTF_8.name());
            }
            else {
                log.info("uso templatePath");
                InputStream tempalteIs = m.getByPath(templatePath);
                templateString = inputStreamToString(tempalteIs);
            }
            StringTemplateLoader stl = new StringTemplateLoader();
            stl.putTemplate("template", templateString);
            cfg.setTemplateLoader(stl);
            
            temp = cfg.getTemplate("template");
            
            baos = new ByteArrayOutputStream();
            Writer out = new OutputStreamWriter(baos);
            temp.process(data, out);

            // For the sake of example, also write output into a file:
            //TODO:  da togliere
//    File forTest = new File("output.html");
//    Writer fileWriter = new FileWriter(forTest);
//    try {
//      temp.process(data, fileWriter);
//    } finally {
//      fileWriter.close();
//    }

//            log.info(baos.toString("UTF-8"));
            
            ITextRenderer renderer = new ITextRenderer();
            renderer.setListener(new PDFCreationListener() {

                @Override
                public void preOpen(ITextRenderer itr) {
                    PdfAWriter writer = (PdfAWriter) itr.getWriter();
                    writer.setPdfVersion(PdfWriter.PDF_VERSION_1_4);
                }

                @Override
                public void onClose(ITextRenderer itr) {
                    PdfAWriter writer = (PdfAWriter) itr.getWriter();
                    try {
   
                        PdfDictionary structureTreeRoot = new PdfDictionary();
                        structureTreeRoot.put(PdfName.TYPE, PdfName.STRUCTTREEROOT);
                        writer.getExtraCatalog().put(PdfName.STRUCTTREEROOT, structureTreeRoot);

                        PdfDictionary markInfo = new PdfDictionary(PdfName.MARKINFO);
                        markInfo.put(PdfName.MARKED, new PdfBoolean(true));
                        writer.getExtraCatalog().put(PdfName.MARKINFO, markInfo);

                        PdfDictionary l = new PdfDictionary(PdfName.LANG);
                        l.put(PdfName.LANG, new PdfBoolean("true"));
                        writer.getExtraCatalog().put(PdfName.LANG, l);

                        ICC_Profile icc = ICC_Profile.getInstance(iccProfileStream);
                        writer.setOutputIntents("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", icc);

                        writer.createXmpMetadata();
                    }
                    catch (Exception ex) {
                        log.error(ex);
                    }
                }

                @Override
                public void preWrite(ITextRenderer itr, int i) {
                }

            });

            renderer.setDocumentFromString(baos.toString("UTF-8"));
            renderer.layout();
            pdfOut = new ByteArrayOutputStream();
//            renderer.createPDF(new FileOutputStream("report_test.pdf"));
            renderer.createPDF(pdfOut, true, true, PdfAConformanceLevel.PDF_A_1A);
            String tempFileName = String.format("reporter_%s.pdf", UUID.randomUUID().toString());
            String outPdfUuidTemp=md.put(new ByteArrayInputStream(pdfOut.toByteArray()), tempFileName, "/tmp", false);
            
            JSONObject res = new JSONObject();
            //res.put("pdf", Base64Coder.encodeLines(pdfOut.toByteArray()));
            res.put("pdf", outPdfUuidTemp);
            
            //TODO:  da togliere
//            forTest.delete();
            
            return res;
//            DataOutputStream dataos = new DataOutputStream(new FileOutputStream("test_trep.pdf"));
//            dataos.write(pdfOut.toByteArray());
//            dataos.close();
//            return null;
        }
        catch (Exception ex) {
            
            File forTest = new File("output_wrong.html");
            Writer fileWriter = null;
            try {
              fileWriter = new FileWriter(forTest);
              if (temp != null && data != null)
                temp.process(data, fileWriter);
            }
            catch (Exception subEx) {
                log.error(subEx);
            }
            finally {
              org.apache.commons.io.IOUtils.closeQuietly(fileWriter);
            }
            
            if (ex instanceof WorkerException)
                throw (WorkerException)ex;
            else {
                throw new WorkerException(getJobType(), ex);
            }
        }
        finally {
            org.apache.commons.io.IOUtils.closeQuietly(pdfOut);
            org.apache.commons.io.IOUtils.closeQuietly(baos);
            org.apache.commons.io.IOUtils.closeQuietly(qrCodeOutputStream);
            // cancellazione file temporaneo di qrcode
            if (qrcodeTmpFile != null) {
                boolean deleteted = qrcodeTmpFile.delete();
            }
        }
        
    }

    /** Converte un InputStream in una stringa
     * 
     * @param is l'InputStream da convertire
     * @return L'inputStream convertito in stringa
     * @throws UnsupportedEncodingException
     * @throws IOException 
     */
    public static String inputStreamToString(InputStream is) throws UnsupportedEncodingException, IOException {
         Writer stringWriter = new StringWriter();
         char[] buffer = new char[1024];
         try {
             Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             int n;
             while ((n = reader.read(buffer)) != -1) {
                 stringWriter.write(buffer, 0, n);
             }
         }
         finally {
         }
         return stringWriter.toString();
    }
    
    @Override
    public final String getJobType() {
        return "Reporter";
    }

//    public static void main(String[] args) throws WorkerException {
//        ReporterWorker rw = new ReporterWorker();
//        rw.doWork(new JSONObject(), 0, new JSONArray(), new File("."), new File("."));
//    }
    
   
}
