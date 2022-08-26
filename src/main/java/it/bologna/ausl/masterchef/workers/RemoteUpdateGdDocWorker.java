package it.bologna.ausl.masterchef.workers;

import it.bologna.ausl.ioda.iodaobjectlibrary.Document.DocumentOperationType;
import it.bologna.ausl.masterchef.Chef;
import static it.bologna.ausl.masterchef.Launcher.config;
import it.bologna.ausl.masterchef.errors.UpdateGdDocErrorDetails;
import it.bologna.ausl.masterchef.errors.WorkerException;
import it.bologna.ausl.masterchef.utils.UtilityFunctions;
import java.io.File;
import java.io.InputStream;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/*
 INPUT
"tipoOperazione": "insert, update o delete, per indicare se il GdDoc andrà inserito, aggiornato o cancellato"
"requestData":
{
  idApplicazione: "idApplicazione",
  tokenApplicazione: "tokenApplicazione",
  object:
  {
    "tipoClasse": "documento" // valore fisso
    "idOggettoOrigine":"guid del documento al quale il gddoc si riferisce"
    "tipoOggettoOrigine":"tipo del documento al quale il gddoc si riferisce, es. DocumentoPico, Determina, Delibera, Verbale"
    "nome":"nome del gddoc"
    "record":"true/false per indicare se il gddoc da creare sarà un record"
    "dataUltimaModifica":"si riferisce alla modifica nei nostri sistemi"
    "visibile":"true/false, indica se il gdDoc deve essere visibile"
    "codiceRegistro":"il codice registro, es. PG, DETE, DELI"
    "dataRegistrazione":"la data di registrazione"
    "numeroRegistrazione":"il numero di registrazione, es. il numero di protocollo o il numero di determina"
    "fascicoli":[ 
                  {
                   "tipoOperazione": "insert o delete, per indicare se il GdDoc andrà inserito o rimosso da fascicolo"
                   "codiceFascicolo": "codice che identifica il univocamente il fascicolo, la numerazione gerarchica nel nostro caso"
                  },
                  ...
    ],
    "sottoDocumenti":[ 
                      {
                        "tipoOperazione":"insert/update o delete. In caso di inserimento del GdDoc tutti i Sottodocumenti devono essere insert"
                        "nome":"nome del SottoDocumento"
                        "codiceSottoDocumento":"codice che identifica univocamente il SottoDocumento"
                        "tipo":"tipo del sotto documento"
                        "tipoFirma": "opzionale (obbligatorio se firmatari.length() > 0) : tipologia di firma: grafometrica, digitale, autografa"
                        "firmatari":[
                                      "nome firmatario 1",
                                      "nome firmatario 2",
                                      ...
                                    ]
                        "principale":"indica se il sottodocumento è da considerarsi come principale del GdDoc"
                        "uuidMongoOriginale":"uuid su mongo del file originale"
                        "uuidMongoFirmato":"uuid su mongo del file firmato"
                        "uuidMongoPdf":"uuid su mongo del pdf"
                        "mimeTypeFileOriginale":"mimeType del file originale"
                        "mimeTypeFileFirmato":"mimeType del file firmato"
                      },
                      ...
    ]
  }
}

OUTPUT
{
} 
    * Cose da ricordare: 
        * l'oggetto "requestData" praticamente è un oggetto IodaRequestDescriptor della libreria IodaObjectLibrary
*/
public class RemoteUpdateGdDocWorker implements Chef {
//private static final String configFile=System.getProperty("ChefWorker.configFile","chefWorker.properties");
//private final Properties config;
private final String REQUEST_DESCRIPTOR_PART_NAME = "request_descriptor";
private static final Logger log = LogManager.getLogger(RemoteUpdateGdDocWorker.class);

    public RemoteUpdateGdDocWorker() throws WorkerException{
//        config=Launcher.loadConfig(configFile);
    }

    @Override
    public JSONObject doWork(JSONObject in, int j, JSONArray otherin, File workDir, File tmpDir) throws WorkerException {
        Response client = null;
        InputStream servletRes = null;
        try {

            String servletUrl = null;
            DocumentOperationType tipoOperazione = DocumentOperationType.fromString((String) in.get("tipoOperazione"));
            switch (tipoOperazione) {
            case INSERT:
                servletUrl = config.getProperty("ioda.document.insert", "http://gdml:9082/bds_tools/ioda/api/document/insert");
//                servletUrl = config.getProperty("ioda.document.insert", "http://localhost:8082/bds_tools/ioda/api/document/insert");
                break;
            case UPDATE:
                servletUrl = config.getProperty("ioda.document.update", "http://gdml:9082/bds_tools/ioda/api/document/update");
                break;
            case DELETE:
                servletUrl = config.getProperty("ioda.document.delete", "http://gdml:9082/bds_tools/ioda/api/document/delete");
            }
            
            
            JSONObject requestData = (JSONObject) in.get("requestDescriptor");
            
//            IodaRequestDescriptor req = IodaRequestDescriptor.parse(requestData.toJSONString());
//            System.out.println(req.getJSONString());
//            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
//            entityBuilder.addTextBody(REQUEST_DESCRIPTOR_PART_NAME, requestData.toJSONString(), ContentType.APPLICATION_JSON);

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart(REQUEST_DESCRIPTOR_PART_NAME, requestData.toJSONString())
                .build();
            
            try {
                client = UtilityFunctions.sendHttpMultipartMessage(servletUrl, requestBody, servletRes);
            }
            catch (HttpResponseException ex) {
                throw new WorkerException(getJobType(), "Errore tornato dal server", new UpdateGdDocErrorDetails(ex.getStatusCode(), ex.getMessage()));
            }

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
            IOUtils.closeQuietly(client);
            IOUtils.closeQuietly(servletRes);
        }
    }

    @Override
    public final String getJobType() {
        return "RemoteUpdateGdDoc";
    }
}
