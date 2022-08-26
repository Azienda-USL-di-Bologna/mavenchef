package it.bologna.ausl.masterchef.workers;

import it.bologna.ausl.masterchef.Chef;
import static it.bologna.ausl.masterchef.Launcher.config;
import it.bologna.ausl.masterchef.errors.WorkerException;
import it.bologna.ausl.utils.albo.AlboManager;
import it.bologna.ausl.utils.albo.DatiTrasparenza;
import java.io.File;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/*
 "params": {
 "idDoc": "idDoc",
 "articolazione": "articolazione",
 "oggettoPub": "oggettoPub",
 "registro": "registro",
 "numero": "numero",
 "dataDoc": "dataDoc",
 "dataEsecutivita": "dataEsecutivita",
 "dataAdozione": "dataAdozione",
 "validita": "validita",
 "anno": "anno",
 "tipoProvvedimento":"tipoProvvedimento", 
 "contenuto":"contenuto", 
 "eventualeSpesaPrevista":"eventualeSpesaPrevista", 
 "estremiDocumentiRelativi":"estremiDocumentiRelativi",
 }

 "res": {
 "status": "success|error"
 "id_pubblicazione": "id_pubblicazione",
 }

 */
public class AlboTrasparenzaWorker implements Chef {

//    private static final String configFile = System.getProperty("ChefWorker.configFile", "chefWorker.properties");
    private final String postUri, username, password;  

    public AlboTrasparenzaWorker() throws WorkerException {
//        Properties config = Launcher.loadConfig(configFile);
        postUri = config.getProperty("albotrasparenza.uri", "http://vm-zope-dev2:8380/eauslbo/albo_pubblica_PROT");
        username = config.getProperty("albotrasparenza.username", "utente");
        password = config.getProperty("albotrasparenza.password", "utente");
    }

    @Override
    public JSONObject doWork(JSONObject in, int i, JSONArray otherin, File workDir, File tmpDir) throws WorkerException {
        
        DatiTrasparenza adoc = new DatiTrasparenza();
        
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
        adoc.setValidita((String) in.get("validita"));
        //TRASPSARENZA
        adoc.setTipoProvvedimento((String) in.get("tipoProvvedimento"));
        adoc.setContenuto((String) in.get("contenuto"));
        adoc.setEventualeSpesaPrevista((String) in.get("eventualeSpesaPrevista"));
        adoc.setEstremiDocumentiRelativi((String) in.get("estremiDocumentiRelativi"));

        
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
                status = (String) jres.get("status");
            }
            if (status != null && status.equalsIgnoreCase("success")) {
                String idPubblicazione = (String) jres.get("id_pubblicazione");                
                jres.put("status", status);
                jres.put("id_pubblicazione", idPubblicazione);
            } else {
                throw new WorkerException(getJobType(), res);
            }
            return jres;

        } catch (Exception ex) {
            throw new WorkerException(getJobType(), ex);
        } 
    }

    @Override
    public final String getJobType() {
        return "AlboTrasparenza";
    }

}
