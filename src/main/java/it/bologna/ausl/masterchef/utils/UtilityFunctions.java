package it.bologna.ausl.masterchef.utils;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.ICC_Profile;
import com.itextpdf.text.pdf.PdfAConformanceLevel;
import com.itextpdf.text.pdf.PdfACopy;
import com.itextpdf.text.pdf.PdfBoolean;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
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
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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

    @Deprecated
    public static byte[] mergePdfOld(ArrayList<InputStream> inputFiles, InputStream iccProfileStream, boolean pdfa) throws DocumentException, IOException {
        InputStream is = null;
        PdfReader reader = null;
        Document document = null;
        PdfCopy cp = null;
        ByteArrayOutputStream tempos = new ByteArrayOutputStream();
        try {
            is = inputFiles.get(0);
            reader = new PdfReader(is);
            document = new Document(reader.getPageSizeWithRotation(1));
            if (pdfa) {
                cp = new PdfACopy(document, tempos, PdfAConformanceLevel.PDF_A_1A);
            } else {
                cp = new PdfCopy(document, tempos);
            }
            document.open();
            for (int i = 0; i < inputFiles.size(); i++) {
                if (i > 0) {
                    is = inputFiles.get(i);
                    reader = new PdfReader(is);
                }
                removeSigns(reader);
                for (int k = 1; k <= reader.getNumberOfPages(); ++k) {
                    cp.addPage(cp.getImportedPage(reader, k));
                }
                cp.freeReader(reader);
                IOUtils.closeQuietly(is);
            }
            if (pdfa) {
                PdfDictionary structureTreeRoot = new PdfDictionary();
                structureTreeRoot.put(PdfName.TYPE, PdfName.STRUCTTREEROOT);
                cp.getExtraCatalog().put(PdfName.STRUCTTREEROOT, structureTreeRoot);

                PdfDictionary markInfo = new PdfDictionary(PdfName.MARKINFO);
                markInfo.put(PdfName.MARKED, new PdfBoolean(true));
                cp.getExtraCatalog().put(PdfName.MARKINFO, markInfo);

                PdfDictionary l = new PdfDictionary(PdfName.LANG);
                l.put(PdfName.LANG, new PdfBoolean("true"));
                cp.getExtraCatalog().put(PdfName.LANG, l);

                ICC_Profile icc = ICC_Profile.getInstance(iccProfileStream);
                cp.setOutputIntents("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", icc);
                cp.createXmpMetadata();
            }
            cp.close();
            document.close();
            return tempos.toByteArray();
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
        }
    }
    
    public static byte[] mergePdf(List<Map.Entry<String, MongoWrapper>> allegati, InputStream iccProfileStream, boolean pdfa) throws DocumentException, IOException, MongoWrapperException {
        InputStream is = null;
        PdfReader reader = null;
        Document document = null;
        PdfCopy cp = null;
        ByteArrayOutputStream tempos = new ByteArrayOutputStream();
        try {
            Map.Entry<String, MongoWrapper> first = allegati.get(0);
            
            is = first.getValue().get(first.getKey());
            reader = new PdfReader(is);
            document = new Document(reader.getPageSizeWithRotation(1));
            if (pdfa) {
                cp = new PdfACopy(document, tempos, PdfAConformanceLevel.PDF_A_1A);
            } else {
                cp = new PdfCopy(document, tempos);
            }
            document.open();
            for (int i = 0; i < allegati.size(); i++) {
                if (i > 0) {
                    Map.Entry<String, MongoWrapper> entry = allegati.get(i);
                    is = entry.getValue().get(entry.getKey());
                    reader = new PdfReader(is);
                }
                removeSigns(reader);
                for (int k = 1; k <= reader.getNumberOfPages(); ++k) {
                    cp.addPage(cp.getImportedPage(reader, k));
                }
                cp.freeReader(reader);
                IOUtils.closeQuietly(is);
            }
            if (pdfa) {
                PdfDictionary structureTreeRoot = new PdfDictionary();
                structureTreeRoot.put(PdfName.TYPE, PdfName.STRUCTTREEROOT);
                cp.getExtraCatalog().put(PdfName.STRUCTTREEROOT, structureTreeRoot);

                PdfDictionary markInfo = new PdfDictionary(PdfName.MARKINFO);
                markInfo.put(PdfName.MARKED, new PdfBoolean(true));
                cp.getExtraCatalog().put(PdfName.MARKINFO, markInfo);

                PdfDictionary l = new PdfDictionary(PdfName.LANG);
                l.put(PdfName.LANG, new PdfBoolean("true"));
                cp.getExtraCatalog().put(PdfName.LANG, l);

                ICC_Profile icc = ICC_Profile.getInstance(iccProfileStream);
                cp.setOutputIntents("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", icc);
                cp.createXmpMetadata();
            }
            cp.close();
            document.close();
            return tempos.toByteArray();
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
        }
    }

    public static void removeSigns(PdfReader reader) {
        try {
            ArrayList<String> signatureNames = reader.getAcroFields().getSignatureNames();
            AcroFields acroFields = reader.getAcroFields();
            for (String signatureName : signatureNames) {
                // se la larghezza e l'altezza del rettangolo in cui la firma è posizionata sono diverso da 0 allora il campo firma è visibile.
                // se è visibile posso rimuovere il campo
                List<AcroFields.FieldPosition> fieldPosition = reader.getAcroFields().getFieldPositions(signatureName);
                if (fieldPosition != null && !fieldPosition.isEmpty()) {
                    Rectangle position = fieldPosition.get(0).position;
                    if (position.getHeight() != 0 || position.getHeight() != 0) {
                        acroFields.removeField(signatureName);
                    }
                }
            }
        } catch (Exception ex) {
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

    public static void main(String[] args) throws UnknownHostException, MongoException, MongoWrapperException, IOException, DocumentException {

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
        ArrayList<InputStream> al = new ArrayList<InputStream>();
        al.add(new FileInputStream("C:\\tmp\\test\\frontespizio.pdf"));
        al.add(new FileInputStream("C:\\tmp\\test\\PG0000376_2020_Lettera_firmata.pdf"));
        al.add(new FileInputStream("c:\\tmp\\test\\2020-1779_Allegato1.pdf"));
        al.add(new FileInputStream("c:\\tmp\\test\\appunti.pdf"));
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

//        byte[] mergePdf;
//        try {
//            mergePdf = mergePdf(al, iccProfileStream, false);
//        } catch (PdfAConformanceException ex) {
//            mergePdf = mergePdf(al, null, false);
//        }
//        writeFileFromBytes(new File("c:\\tmp\\test\\merge.pdf"), mergePdf);
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
