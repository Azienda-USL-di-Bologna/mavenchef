package it.bologna.ausl.masterchef.utils;
import org.openpdf.text.pdf.PdfObject;
import com.mongodb.MongoException;
import it.bologna.ausl.generator.JWTGenerator;
import static it.bologna.ausl.masterchef.Launcher.config;
import it.bologna.ausl.mongowrapper.MongoWrapper;
import it.bologna.ausl.mongowrapper.exceptions.MongoWrapperException;
import it.bologna.ausl.parameters_client.ParametersDbClient;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.lang.JoseException;
import org.json.simple.JSONObject;

/**
 *
 * @author Giuseppe De Marco (gdm)
 */
public class UtilityFunctions {

    private static final Logger log = LogManager.getLogger(UtilityFunctions.class);
    private static final String MONGO_CREATOR_SYSTEM = "system";

    
//    public static byte[] mergePdf(ArrayList<File> allegati, InputStream iccProfileStream, boolean pdfa) throws IOException, MongoWrapperException {
//        ByteArrayOutputStream tempos = new ByteArrayOutputStream();
//        
//        try (PdfWriter writer = new PdfWriter(tempos);
//                PdfDocument mergedDocument = new PdfDocument(writer);) {
//           
//            PdfMerger merger = new PdfMerger(mergedDocument);
//            for (int i = 0; i < allegati.size(); i++) {
//                    File pdfInputStream = allegati.get(i);
//                    try (
//                            PdfReader reader = new PdfReader(pdfInputStream);
//                            PdfDocument pdfDocument = new PdfDocument(reader);) {
////                        PdfDocument pdfDocument1 = new PdfDocument(pdfDocument.getReader());
//                        merger.merge(pdfDocument, 1, pdfDocument.getNumberOfPages());
//                    }
////                removeSigns(pdfDocument);
//            }
//        }
//            return tempos.toByteArray();
//    }
    
//    public static byte[] mergePdf(List<Map.Entry<String, MongoWrapper>> allegati, InputStream iccProfileStream, boolean pdfa) throws IOException, MongoWrapperException {
//        
//        ByteArrayOutputStream tempos = new ByteArrayOutputStream();
//        
//        try (PdfWriter writer = new PdfWriter(tempos);) {
//            PdfDocument merged;
//            if (pdfa) {
//                merged = new PdfADocument(writer, PdfAConformance.PDF_A_1A, new PdfOutputIntent("Custom", "", "https://www.color.org", "sRGB IEC61966-2.1", iccProfileStream));
//            } else {
//                merged = new PdfDocument(writer);
//            }
//            try (merged) {
//                PdfMerger merger = new PdfMerger(merged);
//                for (int i = 0; i < allegati.size(); i++) {
//                    Map.Entry<String, MongoWrapper> entry = allegati.get(i);
//                    InputStream is = entry.getValue().get(entry.getKey());
//                    try (
//                        PdfReader reader = new PdfReader(is);
//                        PdfDocument pdfDocument = new PdfDocument(reader);
//                    ) {
//                        if (pdfa && !isPdfACompliant(pdfDocument)) {
//                            throw new PdfAConformanceException(String.format("il file %s non è dichiarato come pdf/a", entry.getValue()));
//                        }
//                        removeSigns(pdfDocument);
//                        merger.merge(pdfDocument, 1, pdfDocument.getNumberOfPages());
//                    }
//                }
//            }
////            document.open();
//            
//        }
//        return tempos.toByteArray();
//    }
    
    /**
     * Metodo di verifica di conformità PDF/A basato su caratteristiche tipiche dei PDF/A
     * 
    */
//    private static boolean isPdfACompliant(PdfDocument pdfDocument) {
//        try {
//            // Controllo basato su metadati XMP, obbligatori per PDF/A
//            XMPMeta xmpMeta = XMPMetaParser.parse(pdfDocument.getXmpMetadata(), new ParseOptions());
//            
//            String pdfAProperty = xmpMeta.getPropertyString("http://www.aiim.org/pdfa/ns/id/", "pdfaid:part");
//            // Restituisce true solo se "pdfaid:part" è settato, indicando conformità PDF/A
//            return pdfAProperty != null;
//        } catch (Exception e) {
//            log.error("errore nella verifica della conformità");
//            return false;
//        }
//    }

//    public static void removeSigns(PdfDocument pdfDocument) {
//        try {
//            PdfAcroForm acroForm = PdfFormCreator.getAcroForm(pdfDocument, true);
//            acroForm.getAllFormFields().entrySet().stream()
//                .filter(entry -> entry.getValue().getFormType() == PdfName.Sig)
//                .forEach(entry -> acroForm.removeField(entry.getKey()));
//        } catch (Exception ex) {
////            log.error("errore nella rimozione dei campi firma", ex);
//        }
//
//    }

    /**
     * Rimuove i campi firma dal reader. Utile da usare in caso di merge per evitare di avere i campi firma nel risultato
     * @param reader 
     */
    public static void removeSignsOpenPdf(org.openpdf.text.pdf.PdfReader reader) {
        try {
            List<String> signatureNames = reader.getAcroFields().getSignedFieldNames();
            org.openpdf.text.pdf.AcroFields acroFields = reader.getAcroFields();
            for (String signatureName : signatureNames) {
                // se la larghezza e l'altezza del rettangolo in cui la firma è posizionata sono diverso da 0 allora il campo firma è visibile.
                // se è visibile posso rimuovere il campo
                float[] fieldPosition = reader.getAcroFields().getFieldPositions(signatureName);
                if (fieldPosition != null && fieldPosition.length > 0) {
                    acroFields.removeField(signatureName);
                }
            }
        } catch (Exception ex) {
//            ex.printStackTrace();
            log.error("errore nella rimozione dei campi firma", ex);
        }

    }
    
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    /**
     * Crea un file contente il testo passato
     *
     * @param fileToCreate il file da creare
     * @param text il testo da scrivere nel file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void stringToFile(File fileToCreate, String text) throws FileNotFoundException, IOException {
        writeFileFromBytes(fileToCreate, text.getBytes());
    }

    /**
     * Crea un file a partire dai bytes passati
     *
     * @param fileToCreate il file da creare
     * @param bytes i bytes del file da creare
     * @throws java.io.FileNotFoundException
     */
    public static void writeFileFromBytes(File fileToCreate, byte[] bytes) throws FileNotFoundException, IOException {
        DataOutputStream dataos = null;
        try {
            dataos = new DataOutputStream(new FileOutputStream(fileToCreate));
            dataos.write(bytes);
        } finally {
            IOUtils.closeQuietly(dataos);
        }
    }

    public static String removeExtensionFromFileName(String fileName) {
        String res = fileName;
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            res = res.substring(0, pos);
        }
        return res;
    }

    public static String getExtensionFromFileName(String fileName) {
        String res = "";
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            res = fileName.substring(pos + 1, fileName.length());
        }
        return res;
    }

    public static String renameExistentFileMongo(MongoWrapper m, String filePath) throws MongoWrapperException {
        String res = filePath;

        Map<String, Object> metadata = m.getMetadataByPath(filePath);
        if (metadata != null) {
            String creator = (String) metadata.get("creator");
            if (creator == null || !creator.equalsIgnoreCase(MONGO_CREATOR_SYSTEM)) {
                String newFileName;
                int i = 1;
                do {
                    newFileName = removeExtensionFromFileName(filePath) + "_" + i++ + "." + getExtensionFromFileName(filePath);
                } while (m.existsObjectbyPath(newFileName));
                res = newFileName;
                m.move(m.getFidByPath(filePath), newFileName);
            }
        }
        return res;
    }

    /**
     *
     * @param targetUrl
     * @param requestBody entityBuilder con le parti
     * @param output inputStream che conterrà il risultato
     * @return httpClient creato per poterlo chiudere
     * @throws UnsupportedEncodingException
     * @throws HttpResponseException
     * @throws IOException
     */
    public static Response sendHttpMultipartMessage(String targetUrl, RequestBody requestBody, InputStream output) throws IOException, HttpResponseException {

        Request request = new Request.Builder()
                .url(targetUrl)
                .post(requestBody)
                .build();
        
//        ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
//                .tlsVersions(TlsVersion.TLS_1_2)
//                .build();
////
//        List<ConnectionSpec> specs = new ArrayList<>();
//        specs.add(cs);
//        specs.add(ConnectionSpec.COMPATIBLE_TLS);
//        specs.add(ConnectionSpec.CLEARTEXT);
//        
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(2, TimeUnit.MINUTES)
                .connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
//                .connectionSpecs(Collections.singletonList(spec))
//                .connectionSpecs(specs)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new HttpResponseException(response.code(), response.message());
        } else {
            output = response.body().byteStream();
        }
        return response;

//        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//        HttpPost httpPost = new HttpPost(targetUrl);
//        httpPost.setEntity(entityBuilder.build());
//        CloseableHttpClient http = HttpClientBuilder.create().build();
//        http.getConnectionManager().getSchemeRegistry().register(
//                        new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
//                );
//        HttpResponse response = http.execute(httpPost);
//        int responseCode = response.getStatusLine().getStatusCode();
//        String responseCodeToString = String.valueOf(responseCode);
//
//        HttpEntity entity = response.getEntity();
//        if (!responseCodeToString.substring(0, responseCodeToString.length() - 1).equals("20")) {
//            throw new HttpResponseException(responseCode, EntityUtils.toString(entity));
//        } else {
//            output = entity.getContent();
//            return http;
//        }
    }

    /**
     * Converte un InputStream in una stringa
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
            Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                stringWriter.write(buffer, 0, n);
            }
        } finally {
        }
        return stringWriter.toString();
    }

    /**
     * Torna un inputStream della stringa passata
     *
     * @param str
     * @return
     */
    public static InputStream stringToInputStream(String str) {
        try {
            InputStream is = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
            return is;
        } catch (Exception ex) {
            //ex.printStackTrace(System.out);
            return null;
        }
    }

    /**
     * Scrive un InputStream in un file
     *
     * @param inputStream l'InpurStream da scrivere
     * @param fileToCreate il file da creare
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void inputStreamToFile(InputStream inputStream, File fileToCreate) throws FileNotFoundException, IOException {
        OutputStream os = new FileOutputStream(fileToCreate);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
    }

    public static HttpCallResponse httpCallWithHeaders(String url, Map<String, String> headers, String contentType, byte[] body, int timeoutSec, String requestMethod) throws IOException {

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        OkHttpClient client = clientBuilder
                .readTimeout(timeoutSec, TimeUnit.SECONDS)
                .build();

        okhttp3.MediaType mediaType = okhttp3.MediaType.parse(String.format("%s; charset=utf-8", contentType));

        Request.Builder requestBuilder = new Request.Builder();

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        if (requestMethod.equalsIgnoreCase("POST")) {
            RequestBody requestBody = RequestBody.create(mediaType, body);
            requestBuilder.url(url)
                    .post(requestBody);
        }

        if (requestMethod.equalsIgnoreCase("GET")) {
            requestBuilder.get().url(url);
        }

        Request request = requestBuilder.build();
        okhttp3.Response response = client.newCall(request).execute();
        return new HttpCallResponse(response);
    }

    public static String getTokenFromInternauta(String subject) throws IOException {

        String token = null;
        String internautaAuthUri = config.getProperty("internauta.endpoint.authentication.uri", "https://gdml.internal.ausl.bologna.it/internauta-api/endpoint/login");
        String preToken = generateJWS(subject);

        if (preToken != null) {
            JSONObject endpointObject = new JSONObject();
            endpointObject.put("jws", preToken);
            endpointObject.put("applicazione", "gedi");

            HttpCallResponse response = httpCallWithHeaders(internautaAuthUri, null, "application/json", endpointObject.toJSONString().getBytes("UTF-8"), 600, "POST");
            if (response != null) {
                String responseString = response.getString();
                JSONObject responseJson = (JSONObject) org.json.simple.JSONValue.parse(responseString);
                token = (String) responseJson.get("token");
            }
        }

        return token;
    }

    public static String generateJWS(String subject) throws IOException {

        try {
            JWTGenerator jwtGenerator = new JWTGenerator();
            String env = config.getProperty("parer.ambient", "test");
            ParametersDbClient parametersClient = new ParametersDbClient();
            String envsuite = env + "_BABEL";
            JWTGenerator.AMBIENTE ambiente = null;

            switch (envsuite.toUpperCase()) {
                case "TEST_BABEL":
                    ambiente = JWTGenerator.AMBIENTE.TEST_BABEL;
                    break;
                case "PROD_BABEL":
                    ambiente = JWTGenerator.AMBIENTE.PROD_BABEL;
                    break;
            }

            Key key = jwtGenerator.generateKey(ambiente, ParametersDbClient.getJwtGeneratorKeyStorePassword());
            String codiceRegioneAzienda = ParametersDbClient.getRegione() + parametersClient.getAzienda();
            return jwtGenerator.createJWS(key, "BDS CA", null, subject, parametersClient.getAzienda(), env, codiceRegioneAzienda, false);

        } catch (KeyStoreException | FileNotFoundException
                | NoSuchAlgorithmException | CertificateException
                | UnrecoverableKeyException | JoseException ex) {
            return null;
        }
    }

    public static void main(String[] args) throws UnknownHostException, MongoException, MongoWrapperException, IOException {

//        String token = getTokenFromInternauta("");
//        System.out.println(token);
//
        String uuid = "5d9450ecd5de98bad65cbf9d";
//
//        InputStream get1 = getEmlFromInternauta(token, uuid);
//        cmisSaveToDisc(get1, "c:/tmp/file.eml");
//        MongoWrapper m = new MongoWrapper("mongodb://argo:siamofreschi@procton4,procton3/doc?safe=true");
//        String path = "/test/firmispizio_pico.pdf";
//        System.out.println(m.getFidByPath(path));
//        String newName = renameExistentFileMongo(m, path);
//        System.out.println(newName);
//        m.put(m.getByPath(path), "PG0000425_2013_Stampa_unica.pdf", "/Procton/Documenti/2013-2424", true);
//        m.move(m.getFidByPath(path), newName);
        //5190d9fae4b0e49fc09c2c24
//        removeSigns(new PdfReader("C:\\Users\\gdm\\Downloads\\Telegram Desktop\\PG0043013_2018_Incarico_Chirurgia.pdf"));
        ArrayList<File> al = new ArrayList();
//        al.add(new FileInputStream("Frontespizio.pdf"));
//        al.add(new FileInputStream("password.pdf"));
//        al.add(new FileInputStream("test_trep.pdf"));
//        al.add(new FileInputStream("c:\\tmp\\test\\appunti.pdf"));
//        al.add(new FileInputStream("C:\\tmp\\test\\PG0142682_2017_SCIA_RELAZIONE_TECNICA.pdf"));
//        al.add(new FileInputStream("C:\\tmp\\2_gdm\\ccc\\PG0069696_2016_CONFORMITA CUCINA ALPINI09052016.pdf"));
//        al.add(new FileInputStream("C:\\tmp\\2_gdm\\ccc\\PG0069696_2016_festa maccherone 3.pdf"));
//        al.add(new FileInputStream("C:\\tmp\\2_gdm\\ccc\\PG0069696_2016_Planimetria_da allegare.pdf 1_gdm.pdf"));
//        al.add(new FileInputStream("C:\\tmp\\2_gdm\\ccc\\PG0069696_2016_Posta Unione Montana Valli Savena-Idice - Fwd_ I_ CORTESE ATTENZIONE ARCH.pdf"));
//        al.add(new FileInputStream("C:\\tmp\\2_gdm\\ccc\\PG0069696_2016_Relazione lavorazione tecnica alimentiPAOL.pdf"));
//        al.add(new FileInputStream("C:\\tmp\\2_gdm\\ccc\\PG0069696_2016_PLANIMETRIA30042016.pdf"));
//        al.add(new FileInputStream("C:\\tmp\\2_gdm\\ccc\\PG0069696_2016_Frontespizio.pdf"));
//        al.add(new FileInputStream("C:\\tmp\\2_gdm\\ccc\\PG0069696_2016_festa maccherone 3.p7m_segnaposto.pdf"));
//        al.add(new FileInputStream("C:\\tmp\\2_gdm\\ccc\\PG0069696_2016_DOMANDA FESTA PAOL.pdf_segnaposto.pdf"));
        InputStream iccProfileStream = UtilityFunctions.class.getResourceAsStream("it/bologna/ausl/mavenchef/AdobeRGB1998.icc");
        Stream<Path> files = Files.list(Path.of("1"));
        files.forEach(p -> {
            try {
                System.out.println(p.getFileName().toString());
                al.add(p.toFile());
                System.out.println("--------------------");
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(UtilityFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        byte[] mergePdf;
//        try {
            mergePdf = mergePdfOpenPdf(al, iccProfileStream, "titolo");
//        }
//        catch (PdfAConformanceException ex) {
//            mergePdf = mergePdf(al, null, false);
//        }
        writeFileFromBytes(new File("merge.pdf"), mergePdf);
    }

    public static InputStream getEmlFromInternauta(String token, String uuid) throws IOException {

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
    
    public static String generateRandomString(int lenght) {
        String randomString = new Random().ints(lenght, 0, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length())
        .mapToObj(i -> "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".charAt(i))
        .collect(StringBuilder::new, StringBuilder::append, (stringBuilder, s) -> stringBuilder.append(s))
        .toString();
        return randomString;
    }
    
    public static byte[] mergePdfOpenPdf(List<Map.Entry<String, MongoWrapper>> allegati, InputStream iccProfileStream, String title) throws IOException, MongoWrapperException {
        boolean pdfa = true;
        InputStream is = null;
        org.openpdf.text.pdf.PdfReader reader = null;
        org.openpdf.text.Document document = null;
        org.openpdf.text.Document documentPdfA = null;
        org.openpdf.text.pdf.PdfCopy cp = null;
        org.openpdf.text.pdf.PdfCopy cpPdfA = null;
        ByteArrayOutputStream tempos = new ByteArrayOutputStream();
        ByteArrayOutputStream temposPdfA = new ByteArrayOutputStream();
        try {
            Map.Entry<String, MongoWrapper> entry = allegati.get(0);
            is = entry.getValue().get(entry.getKey());
            reader = new org.openpdf.text.pdf.PdfReader(is);
            pdfa = pdfa && isPdfAMarked(reader);
            
            if (pdfa) {
                documentPdfA = new org.openpdf.text.Document(reader.getPageSizeWithRotation(1));
                cpPdfA = new org.openpdf.text.pdf.PdfCopy(documentPdfA, temposPdfA);
                cpPdfA.setPDFXConformance(org.openpdf.text.pdf.PdfWriter.PDFA1A);
                cpPdfA.setPdfVersion(org.openpdf.text.pdf.PdfWriter.PDF_VERSION_1_7);
                documentPdfA.open();
            } else {
                IOUtils.close(temposPdfA);
                temposPdfA = null;
            }
            document = new org.openpdf.text.Document(reader.getPageSizeWithRotation(1));
            cp = new org.openpdf.text.pdf.PdfCopy(document, tempos);
            document.open();
            for (int i = 0; i < allegati.size(); i++) {
                if (i > 0) {
                    entry = allegati.get(i);
                    is = entry.getValue().get(entry.getKey());
                    reader = new org.openpdf.text.pdf.PdfReader(is);
                }
                pdfa = pdfa && isPdfAMarked(reader);
                if (!pdfa && temposPdfA != null) {
                    IOUtils.close(temposPdfA);
                    temposPdfA = null;
                }
//                removeSigns(reader);
                for (int k = 1; k <= reader.getNumberOfPages(); ++k) {
                    cp.addPage(cp.getImportedPage(reader, k));
                    if (pdfa) {
                        cpPdfA.addPage(cpPdfA.getImportedPage(reader, k));
                    }
                }
                cp.freeReader(reader);
                if (pdfa) {
                    cpPdfA.freeReader(reader);
                }
                IOUtils.closeQuietly(is);
            }
            if (title == null) {
                title = reader.getInfo().get(org.openpdf.text.pdf.PdfName.decodeName(org.openpdf.text.pdf.PdfName.TITLE.toString()));
            }            cp.getInfo().put(org.openpdf.text.pdf.PdfName.TITLE, new org.openpdf.text.pdf.PdfString(title));
            if (pdfa) {
                cpPdfA.getInfo().put(org.openpdf.text.pdf.PdfName.TITLE, new org.openpdf.text.pdf.PdfString(title));
            }
            if (pdfa) {
//                String toString = reader.getInfo().get(org.openpdf.text.pdf.PdfName.TITLE).toString();
                writeExtraCatalog(cpPdfA, iccProfileStream);
                writeXmpMetadata(cpPdfA, iccProfileStream);
            }
            cp.close();
            if (pdfa) {
                cpPdfA.close();
                documentPdfA.close();
            }
            document.close();
            if (pdfa) {
                return temposPdfA.toByteArray();
            } else {
                return tempos.toByteArray();
            }
        } finally {
            IOUtils.closeQuietly(is);
            try {
                reader.close();
            } catch (Exception ex) {
            }
            try {
                document.close();
            } catch (Exception ex) {
            }
            try {
                cp.close();
            } catch (Exception ex) {
            }
            if (pdfa) {
                try {
                    cpPdfA.close();
                } catch (Exception e) {
                }
                try {
                    documentPdfA.close();
                } catch (Exception e) {
                }
                IOUtils.closeQuietly(tempos);
            } else {
                IOUtils.closeQuietly(temposPdfA);
            }
        }
    }
    
    public static byte[] mergePdfOpenPdf(ArrayList<File> inputFiles, InputStream iccProfileStream, String title) throws IOException {
        boolean pdfa = true;
        InputStream is = null;
        org.openpdf.text.pdf.PdfReader reader = null;
        org.openpdf.text.Document document = null;
        org.openpdf.text.Document documentPdfA = null;
        org.openpdf.text.pdf.PdfCopy cp = null;
        org.openpdf.text.pdf.PdfCopy cpPdfA = null;
        ByteArrayOutputStream tempos = new ByteArrayOutputStream();
        ByteArrayOutputStream temposPdfA = new ByteArrayOutputStream();
        try {
            is = new FileInputStream(inputFiles.get(0));
            reader = new org.openpdf.text.pdf.PdfReader(is);
            removeSignsOpenPdf(reader);
            pdfa = pdfa && isPdfAMarked(reader);
            
            if (pdfa) {
                documentPdfA = new org.openpdf.text.Document(reader.getPageSizeWithRotation(1));
                cpPdfA = new org.openpdf.text.pdf.PdfCopy(documentPdfA, temposPdfA);
                cpPdfA.setPDFXConformance(org.openpdf.text.pdf.PdfWriter.PDFA1A);
                cpPdfA.setPdfVersion(org.openpdf.text.pdf.PdfWriter.PDF_VERSION_1_7);
                documentPdfA.open();
            } else {
                IOUtils.close(temposPdfA);
                temposPdfA = null;
            }
            document = new org.openpdf.text.Document(reader.getPageSizeWithRotation(1));
            cp = new org.openpdf.text.pdf.PdfCopy(document, tempos);
            document.open();
            for (int i = 0; i < inputFiles.size(); i++) {
                if (i > 0) {
                    is = new FileInputStream(inputFiles.get(i));
                    reader = new org.openpdf.text.pdf.PdfReader(is);
                }
                pdfa = pdfa && isPdfAMarked(reader);
                if (!pdfa && temposPdfA != null) {
                    IOUtils.close(temposPdfA);
                    temposPdfA = null;
                }
                removeSignsOpenPdf(reader);
                for (int k = 1; k <= reader.getNumberOfPages(); ++k) {
                    cp.addPage(cp.getImportedPage(reader, k));
                    if (pdfa) {
                        cpPdfA.addPage(cpPdfA.getImportedPage(reader, k));
                    }
                }
                cp.freeReader(reader);
                if (pdfa) {
                    cpPdfA.freeReader(reader);
                }
                IOUtils.closeQuietly(is);
            }
            if (title == null) {
                title = reader.getInfo().get(org.openpdf.text.pdf.PdfName.decodeName(org.openpdf.text.pdf.PdfName.TITLE.toString()));
            }
            cp.getInfo().put(org.openpdf.text.pdf.PdfName.TITLE, new org.openpdf.text.pdf.PdfString(title));
            if (pdfa) {
                cpPdfA.getInfo().put(org.openpdf.text.pdf.PdfName.TITLE, new org.openpdf.text.pdf.PdfString(title));
            }
            if (pdfa) {
                writeExtraCatalog(cpPdfA, iccProfileStream);
                writeXmpMetadata(cpPdfA, iccProfileStream);
            }
            cp.close();
            if (pdfa) {
                cpPdfA.close();
                documentPdfA.close();
            }
            document.close();
            if (pdfa) {
                return temposPdfA.toByteArray();
            } else {
                return tempos.toByteArray();
            }
        } finally {
            IOUtils.closeQuietly(is);
            try {
                reader.close();
            } catch (Exception ex) {
            }
            try {
                document.close();
            } catch (Exception ex) {
            }
            try {
                cp.close();
            } catch (Exception ex) {
            }
            if (pdfa) {
                try {
                    cpPdfA.close();
                } catch (Exception e) {
                }
                try {
                    documentPdfA.close();
                } catch (Exception e) {
                }
                IOUtils.closeQuietly(tempos);
            } else {
                IOUtils.closeQuietly(temposPdfA);
            }
        }
    }
    
    public static void writeExtraCatalog(org.openpdf.text.pdf.PdfWriter writer, InputStream iccProfileStream) throws IOException{
                        
        org.openpdf.text.pdf.PdfDictionary structureTreeRoot = new org.openpdf.text.pdf.PdfDictionary();
        structureTreeRoot.put(org.openpdf.text.pdf.PdfName.TYPE, org.openpdf.text.pdf.PdfName.STRUCTTREEROOT);
        writer.getExtraCatalog().put(org.openpdf.text.pdf.PdfName.STRUCTTREEROOT, structureTreeRoot);

        org.openpdf.text.pdf.PdfDictionary markInfo = new org.openpdf.text.pdf.PdfDictionary(org.openpdf.text.pdf.PdfName.MARKINFO);
        markInfo.put(org.openpdf.text.pdf.PdfName.MARKED, new org.openpdf.text.pdf.PdfBoolean(true));
        writer.getExtraCatalog().put(org.openpdf.text.pdf.PdfName.MARKINFO, markInfo);

        org.openpdf.text.pdf.PdfDictionary l = new org.openpdf.text.pdf.PdfDictionary(org.openpdf.text.pdf.PdfName.LANG);
        l.put(org.openpdf.text.pdf.PdfName.LANG, new org.openpdf.text.pdf.PdfBoolean("true"));
        writer.getExtraCatalog().put(org.openpdf.text.pdf.PdfName.LANG, l);

        java.awt.color.ICC_Profile icc = java.awt.color.ICC_Profile.getInstance(iccProfileStream);
        writer.setOutputIntents("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", icc);
    }
    
    public static void writeXmpMetadata(org.openpdf.text.pdf.PdfWriter writer, InputStream iccProfileStream) {
        try {
            java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            String modifyDatePlaceHolder = "xmp:ModifyDate=\"[ModifyDate]\"";
            String xmpMetadata = """
                <?xpacket begin="\u00ef\u00bb\u00bf" id="W5M0MpCehiHzreSzNTczkc9d"?>
                    <x:xmpmeta xmlns:x="adobe:ns:meta/" x:xmptk="Adobe XMP Core 5.1.0-jc003">
                        <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                            <rdf:Description 
                                rdf:about=""
                                xmlns:dc="http://purl.org/dc/elements/1.1/"
                                xmlns:pdf="http://ns.adobe.com/pdf/1.3/"
                                xmlns:xmp="http://ns.adobe.com/xap/1.0/"
                                xmlns:pdfaid="http://www.aiim.org/pdfa/ns/id/"
                                dc:format="application/pdf"
                                pdf:Producer="[Producer]"
                                xmp:CreateDate="[CreateDate]"
                                [ModifyDatePlaceHolder]
                                pdfaid:part="1"
                                pdfaid:conformance="A">
                                <dc:title>
                                    <rdf:Alt>
                                        <rdf:li xml:lang="x-default">[title]</rdf:li>
                                    </rdf:Alt>
                                </dc:title>
                            </rdf:Description>
                        </rdf:RDF>
                    </x:xmpmeta>
                <?xpacket end="w"?>
            """;

            // id
            xmpMetadata = xmpMetadata.replace("[id]", generateRandomString(24));

            // CreateDate
            Date creationDate = org.openpdf.text.pdf.PdfDate.decode(writer.getInfo().get(org.openpdf.text.pdf.PdfName.CREATIONDATE).toString()).getTime();
            String creationDateString = dateFormatter.format(creationDate);
            xmpMetadata = xmpMetadata.replace("[CreateDate]", creationDateString);

            // ModifyDate
            org.openpdf.text.pdf.PdfObject modifyDateObj = writer.getInfo().get(org.openpdf.text.pdf.PdfName.MODDATE);
            if (modifyDateObj != null) {
                org.openpdf.text.pdf.PdfDate.decode(modifyDateObj.toString()).getTime();
                String modifyDateString = dateFormatter.format(modifyDateObj);
                xmpMetadata = xmpMetadata
                    .replace("[ModifyDatePlaceHolder]", modifyDatePlaceHolder)
                    .replace("[ModifyDate]", modifyDateString);

            } else {
                xmpMetadata = xmpMetadata.replaceAll(".*\\[ModifyDatePlaceHolder\\]\n", "");
            }

            // title
            PdfObject titleObject = writer.getInfo().get(org.openpdf.text.pdf.PdfName.TITLE);
            String title = titleObject != null ? titleObject.toString(): "NO TITLE";
            xmpMetadata = xmpMetadata.replace("[title]", title);

            // producer
            PdfObject producerObject = writer.getInfo().get(org.openpdf.text.pdf.PdfName.PRODUCER);
            String producer = producerObject != null ? producerObject.toString(): "OpenPdf";
            xmpMetadata = xmpMetadata.replace("[Producer]", producer);

            writer.setXmpMetadata(xmpMetadata.getBytes());    
        } catch (Exception ex) {
            log.error("errore nella crezione degli XMPMetadata", ex);
        }
    }
    
    public static boolean isPdfAMarked(org.openpdf.text.pdf.PdfReader pdfReader) {
        try {
            if (pdfReader.getMetadata() != null) {
                String regex = "pdfaid:conformance\\s*(>?\\s*|=\\s*\\\"?)\\s*A";
                Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(new String(pdfReader.getMetadata()));
                return matcher.find();
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }
    
    public static boolean isAllPdfAMarked(ArrayList<InputStream> files) {
        boolean isAllPdfAMarked = false;
        try {
            for (InputStream file : files) {
                try (org.openpdf.text.pdf.PdfReader reader = new org.openpdf.text.pdf.PdfReader(file)) {
                    isAllPdfAMarked = isPdfAMarked(reader);
                    if (!isAllPdfAMarked) {
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            isAllPdfAMarked = false;
        }
        return isAllPdfAMarked;
    }

//    public static void cmisSaveToDisc(InputStream is, String path) throws IOException {
//        File f = new File(path);
//        OutputStream out = new FileOutputStream(f);
//        byte buf[] = new byte[1024];
//        int len;
//        while ((len = is.read(buf)) > 0) {
//            out.write(buf, 0, len);
//        }
//
//        out.close();
//        is.close();
//    }
}
