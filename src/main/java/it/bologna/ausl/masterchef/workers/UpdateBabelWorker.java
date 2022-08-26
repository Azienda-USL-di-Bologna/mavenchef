package it.bologna.ausl.masterchef.workers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.bologna.ausl.masterchef.Chef;
import static it.bologna.ausl.masterchef.Launcher.config;
import it.bologna.ausl.masterchef.utils.Base64Coder;
import it.bologna.ausl.masterchef.errors.WorkerException;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/*
 INPUT
updatebabeljob={
        'appID':"rediscli",
        'jobID':"1",
        'jobList':[ {
            'jobN':"job1",
            'jobType':"UpdateBabel",
            'params': {
                    'idApplicazione': idApplicazione
                    'tokenApplicazione': tokenApplicazione
                    'setAttivita': setAttivita
                    'archiviazione': archiviazione
                    'accessoEsclusivo': accessoEsclusivo
                    'actionType': (insert|update|delete|update_mult|update_per_idesterno|delete_mult|delete_per_idesterno|allinea)

                    'listaAttivita' : [ {
                        'idAttivita': idattivita
                        'idEsterno': idesterno
                        'idUtenti': idUtente
                        'tipoAttivita': tipoAttivita
                        'descrizioneAttivita': descrizioneAttivita
                        'oggettoAttivita': oggettoAttivita
                        'provenienza': provenienza
                        'priorita':  priorita
                        'labelUrlCommand': labelUrlCommand
                        'urlCommand': urlCommand
                        'labelUrlCommand2': labelUrlCommand2
                        'urlCommand2': urlCommand2
                        'labelUrlCommand3': labelUrlCommand3
                        'urlCommand3': urlCommand3
                        'noteAttivita': noteAttivita
                        'uuidAnteprima': uuidAnteprima
                        }
                    ]
            }
        }],
        'returnQueue':"queueName"
    }
    * Cose da ricordare:
    * in un insert i parametri obbligatori sono: idAttivita, idEsterno, idUtente
    * in un update e delete (update/delete per idAttivita) i parametri obbligatori sono: idAttivita
    * in un update_mult e delete_mult (update/delete con like per idAttivita) i parametri obbligatori sono: idAttivita
    * in un update_per_idesterno e delete_per_idesterno (update/delete per idEsterno) i parametri obbligatori sono: idEsterno
    * in un allinea i parametri obbligatori sono: (nelle attività, almeno una e in ognuna: idAttivita, idEsterno e IdUtente), setattivita, archiviazione


OUTPUT
{
}
 */
public class UpdateBabelWorker implements Chef {
//private static final String configFile=System.getProperty("ChefWorker.configFile","chefWorker.properties");

    private static final Logger log = LogManager.getLogger(UpdateBabelWorker.class);

    private final String INSERT = "insert";
    private final String UPDATE = "update";
    private final String UPDATE_MULT = "update_mult";
    private final String UPDATE_PER_ID_ESTERNO = "update_per_idesterno";
    private final String UPDATE_PER_ID_ESTERNO_E_ID_UTENTE = "update_per_idesterno_e_idutente";
    private final String DELETE = "delete";
    private final String DELETE_MULT = "delete_mult";
    private final String DELETE_PER_ID_ESTERNO = "delete_per_idesterno";
    private final String DELETE_PER_ID_ESTERNO_E_ID_UTENTE = "delete_per_idesterno_e_idutente";
    private final String ALLINEA = "allinea";

    private final String updateBabelSevletUrl;
    private final String updateBabelSevletMultiUrl;

    private static final Integer CONNECTION_TIMEOUT = 600;
    private static final Integer WRITE_TIMEOUT = 600;
    private static final Integer READ_TIMEOUT = 600;

    public UpdateBabelWorker() throws WorkerException {
//        Properties config=Launcher.loadConfig(configFile);
        updateBabelSevletUrl = config.getProperty("updatebabelservlet.url", "http://gdml:9081/babelmanager/UpdateBabel");
        updateBabelSevletMultiUrl = config.getProperty("updatebabelservletmulti.url", "http://gdml:9081/babelmanager/UpdateBabelMulti");
    }

    @Override
    public JSONObject doWork(JSONObject in, int j, JSONArray otherin, File workDir, File tmpDir) throws WorkerException {
        Map<String, String> servletParameters;
        List<Map<String, String>> list = new ArrayList<>();

        String idApplicazione = (String) in.get("idApplicazione");
        String tokenApplicazione = (String) in.get("tokenApplicazione");
        String setAttivita = (String) in.get("setAttivita");
        String archiviazione = (String) in.get("archiviazione");
        String accessoEsclusivo = (String) in.get("accessoEsclusivo");
        String actionType = (String) in.get("actionType");
        JSONArray listaAttivita = (JSONArray) in.get("listaAttivita");

        if (idApplicazione == null || idApplicazione.equals("")) {
            throw new WorkerException(getJobType(), "idApplicazione mancante");
        } else if (tokenApplicazione == null || tokenApplicazione.equals("")) {
            throw new WorkerException(getJobType(), "tokenApplicazione mancante");
        } else if (actionType == null || actionType.equals("")) {
            throw new WorkerException(getJobType(), "actionType mancante");
        }

        log.info("actionType: " + actionType);
        if (actionType.equals(ALLINEA)) {
            if (setAttivita == null || setAttivita.equals("")) {
                throw new WorkerException(getJobType(), "setAttivita mancante");
            } else if (archiviazione == null || archiviazione.equals("")) {
                archiviazione = "false";
            }
            servletParameters = new HashMap<>();
            servletParameters.put("idapplicazione", idApplicazione);
            servletParameters.put("tokenapplicazione", tokenApplicazione);
            servletParameters.put("setattivita", setAttivita);
            servletParameters.put("archiviazione", archiviazione);
            if (accessoEsclusivo != null && !accessoEsclusivo.equals("")) {
                servletParameters.put("setaccessoesclusivo", accessoEsclusivo);
            }
            servletParameters.put("attivita", listaAttivita.toJSONString());

            Set keySet = servletParameters.keySet();
            Iterator keySetIterator = keySet.iterator();
            log.info("Parametri da inviare alla servlet UpdateBabelMulti: ");
            while (keySetIterator.hasNext()) {
                String key = (String) keySetIterator.next();
                String value = (String) servletParameters.get(key);
                log.info(key + ": " + value);
                servletParameters.put(key.toLowerCase(), value);
            }
            try {
                sendHttpMessage(updateBabelSevletMultiUrl, null, null, servletParameters, "POST");
            } catch (Exception ex) {
                throw new WorkerException(getJobType(), ex);
            }
        } else {
            if (listaAttivita != null && listaAttivita.size() > 0) {
                for (Object attivita : listaAttivita) {
                    servletParameters = new HashMap<>();
                    servletParameters.put("idapplicazione", idApplicazione);
                    servletParameters.put("tokenapplicazione", tokenApplicazione);
                    if (setAttivita != null && !setAttivita.equals("")) {
                        servletParameters.put("setattivita", setAttivita);
                    }
                    if (accessoEsclusivo != null && !accessoEsclusivo.equals("")) {
                        servletParameters.put("setaccessoesclusivo", accessoEsclusivo);
                    }
                    if (archiviazione != null && !archiviazione.equals("")) {
                        servletParameters.put("archiviazione", archiviazione);
                    }
                    if (actionType != null && !actionType.equals("")) {
                        servletParameters.put("actiontype", actionType);
                    }
                    JSONObject attivitaMap = (JSONObject) attivita;
                    Set keySet = attivitaMap.keySet();
                    Iterator keySetIterator = keySet.iterator();
                    log.info("Parametri da inviare alla servlet UpdateBabel:");
                    while (keySetIterator.hasNext()) {
                        String key = (String) keySetIterator.next();
                        String value = (String) attivitaMap.get(key);
                        log.info(key + ": " + value);
                        servletParameters.put(key.toLowerCase(), value);
                    }
                    list.add(servletParameters);
                }
                try {
                    Gson gson = new GsonBuilder().create();
                    String jsonString = gson.toJson(list);
//                    Map<String, String> map = new HashMap<>();
//                    map.put("list", jsonString);
                    //sendHttpMessage(updateBabelSevletUrl, null, null, map, "POST");
                    log.info("jsonString: " + jsonString);
                    doPostHttpMessage(updateBabelSevletUrl, jsonString);

                } catch (Exception ex) {
                    throw new WorkerException(getJobType(), ex);
                }
            }
        }

        JSONObject res = new JSONObject();
        return res;
    }

    private int sendHttpMessage(String targetUrl, String username, String password, Map<String, String> parameters, String method) throws MalformedURLException, IOException {

        String parametersToSend = "";
        if (parameters != null) {
            Set<Map.Entry<String, String>> entrySet = parameters.entrySet();
            Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> param = iterator.next();
                String paramName = param.getKey();
                String paramValue = param.getValue();
                parametersToSend += paramName + "=" + URLEncoder.encode(paramValue, "UTF-8");
                if (iterator.hasNext()) {
                    parametersToSend += "&";
                }
            }
            parametersToSend = parametersToSend.replace(" ", "%20");
        }
        URL url = new URL(targetUrl);
        method = method.toUpperCase();
        if (method.equals("GET") || method.equals("DELETE")) {
            if (parametersToSend.length() > 0) {
                targetUrl += "?" + parametersToSend;
            }
            url = new URL(targetUrl);
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        if (username != null && !username.equals("")) {
            String userpassword;
            if (password != null) {
                userpassword = username + ":" + password;
            } else {
                userpassword = "restuser";
            }
            String encodedAuthorization = Base64Coder.encodeString(userpassword);
            connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
        }

        if (method.equals("POST")) {
            connection.setDoOutput(true);
        }
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("charset", "utf-8");
        if (method.equals("POST")) {
            connection.setRequestProperty("Content-Length", "" + Integer.toString(parametersToSend.getBytes().length));
        }
        connection.setUseCaches(false);

        if (method.equals("POST")) {
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.writeBytes(parametersToSend);
                wr.flush();
            }
        }

        int responseCode;
        try (InputStream resultStream = connection.getInputStream()) {
            responseCode = connection.getResponseCode();
        }
        connection.disconnect();
        return responseCode;
    }

    private String doPostHttpMessage(String url, String jsonStr) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, jsonStr);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    @Override
    public final String getJobType() {
        return "UpdateBabel";
    }

}
