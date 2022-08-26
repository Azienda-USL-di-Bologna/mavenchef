package it.bologna.ausl.masterchef.workers;

import it.bologna.ausl.masterchef.Chef;
import static it.bologna.ausl.masterchef.Launcher.config;
import it.bologna.ausl.masterchef.errors.UploadGdDocErrorDetails;
import it.bologna.ausl.masterchef.errors.WorkerException;
import java.io.File;
import java.util.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/*
 INPUT
{
    'idApplicazione': idApplicazione
    'tokenApplicazione': tokenApplicazione
    'idFascicolo': idFascicolo
    'file' : 
        { 'name' : name
          'value': file(base64)
        }
}


OUTPUT
{
} 
    * Cose da ricordare: 
*/
public class UploadGdDocWorker implements Chef {
//private static final String configFile=System.getProperty("ChefWorker.configFile","chefWorker.properties");
private final String uploadGdDocUrl;
private static final Logger log = LogManager.getLogger(UploadGdDocWorker.class);

    public UploadGdDocWorker() throws WorkerException{
//        Properties config=Launcher.loadConfig(configFile);
//        uploadGdDocUrl = config.getProperty("uploadgddocservlet.url","http://localhost:8082/bds_tools/UploadGdDocInFascicolo");
        uploadGdDocUrl = config.getProperty("uploadgddocservlet.url","http://gdml:9081/bds_tools/UploadGdDocInFascicolo");
    }
    
    @Override
    public JSONObject doWork(JSONObject in, int j, JSONArray otherin, File workDir, File tmpDir) throws WorkerException {
        CloseableHttpClient conn = null;
        try {

            log.info("esecuzione del mestiere: " + getJobType());
            String idApplicazione = (String) in.get("idApplicazione");
            String tokenApplicazione = (String) in.get("tokenApplicazione");
            String idFascicolo = (String) in.get("idFascicolo");
            JSONObject file = (JSONObject) in.get("file");
            String fileName = (String) file.get("name");
            String fileValue = (String) file.get("value");
            
            if (idApplicazione == null || idApplicazione.equals("")) {
                throw new WorkerException(getJobType(), "manca il parametro idApplicazione");
            }
            else if (tokenApplicazione == null || tokenApplicazione.equals("")) {
                throw new WorkerException(getJobType(), "manca il parametro tokenApplicazione");
            }
            else if (idFascicolo == null || idFascicolo.equals("")) {
                throw new WorkerException(getJobType(), "manca il parametro idFascicolo");
            }
            else if (file == null) {
                throw new WorkerException(getJobType(), "manca il file");
            }
            
            else if (fileName == null || fileName.equals("")) {
                throw new WorkerException(getJobType(), "manca il parametro name del fle");
            }

            else if (fileValue == null || fileValue.equals("")) {
                throw new WorkerException(getJobType(), "manca il parametro value del file");
            }

//            ByteBuffer decodeByteBuffer = new BASE64Decoder().decodeBufferToByteBuffer(fileValue);
//            ByteBufferBackedInputStream backedInputStream = new ByteBufferBackedInputStream(decodeByteBuffer);
            byte[] decode = Base64.getDecoder().decode(fileValue);


            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);


            entityBuilder.addPart("idapplicazione", new StringBody(idApplicazione, ContentType.TEXT_PLAIN));
            entityBuilder.addPart("tokenapplicazione", new StringBody(tokenApplicazione, ContentType.TEXT_PLAIN));
            entityBuilder.addPart("idfascicolo", new StringBody(idFascicolo, ContentType.TEXT_PLAIN));
            entityBuilder.addPart("file", new ByteArrayBody(decode, fileName));
//            entityBuilder.addTextBody("tokenApplicazione", tokenApplicazione);
//            entityBuilder.addTextBody("idFascicolo", idFascicolo);
//            entityBuilder.addBinaryBody("file", backedInputStream);

            HttpPost httpPost = new HttpPost(uploadGdDocUrl);
            httpPost.setEntity(entityBuilder.build());

            HttpClientBuilder builder = HttpClientBuilder.create();
            conn = builder.build();
            HttpResponse response = null;
            try {
                conn.getConnectionManager().getSchemeRegistry().register(
                    new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
                );
                response = conn.execute(httpPost);
                int statusCode = response.getStatusLine().getStatusCode();
                log.info(getClass().getSimpleName() + " - satus code: " + statusCode);
                if (statusCode < 200 || statusCode >= 300) {
                    throw new HttpResponseException(statusCode, EntityUtils.toString(response.getEntity()));
                }
            }
            catch (HttpResponseException ex) {
                throw new WorkerException(getJobType(), "Errore tornato dal server", new UploadGdDocErrorDetails(ex.getStatusCode(), ex.getMessage()));
            }
            EntityUtils.consume(response.getEntity());

            JSONObject res = new JSONObject();
            return res;
        }
        catch (Exception ex) {
            if (ex instanceof WorkerException)
                throw (WorkerException) ex;
            else
                throw new WorkerException(getJobType(), ex);
        }
        finally {
            try {
                if (conn != null)
                    conn.close();
            }
            catch (Exception ex) {
            }
        }
    }

    @Override
    public final String getJobType() {
        return "UploadGdDoc";
    }
}
