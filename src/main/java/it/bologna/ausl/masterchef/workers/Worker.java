package it.bologna.ausl.masterchef.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.bologna.ausl.masterchef.Chef;
import it.bologna.ausl.masterchef.errors.WorkerException;
import it.bologna.ausl.redisqueue.RedisQueueManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Worker extends Thread {
 private static final Logger log = LogManager.getLogger(Worker.class);
    
private final File workDir;
private final File tmpDir;
private final RedisQueueManager redis;
private boolean exit=false;
private String returnQueue=null;
private Long returnQueueTimeout = null;
URLClassLoader cl=null;

    public Worker(String workDir, String tmpDir, RedisQueueManager redis) throws WorkerException {
        this.workDir = new File(workDir);
        this.tmpDir = new File(tmpDir);
        if (this.workDir.exists() && !this.workDir.isDirectory()) {
            throw new WorkerException(null, "La directory di lavoro passata non è una directory");
        }
        else if (!this.workDir.exists()) {
            this.workDir.mkdir();
        }    
        if (this.tmpDir.exists() && !this.tmpDir.isDirectory()) {
            throw new WorkerException(null, "La directory temporanea passata non è una directory");
        }
        else if (!this.tmpDir.exists()) {
            this.tmpDir.mkdir();
        } 
        this.redis = redis;
//        Logger.getLogger("net.sf.jasperreports").setLevel(Level.ERROR);
//        Logger.getLogger("org.apache").setLevel(Level.ERROR);
    }

    public void exit() {
        this.exit=true;
    }
    /*
        INPUT
        { jobID:String*,
        appID:String*,
        returnQueue:String,
        returnQueueTimeout:int, (opzionale - timeout in secondi che indica dopo quanto tempo la coda deve essere cancellata)
        jobList:[
                {
                jobN:String*,
                jobType:String*,
                params:{}*
                }
        ]
        }
        OUTPUT
        {
            status:OK|KO,
            error:String,
            appID:String,
            jobID:String,
            results:[
            {jobN:String,jobType:String,res:{}}
            ]
        }
*/

    private JSONObject handleMessage(String m) throws Exception {
        //parsare
        JSONObject vals=(JSONObject) JSONValue.parse(m);
        if (vals==null)
            return null;
        //System.out.println(vals.toJSONString());
        //verificare
        Object jobID=null;
        Object appID=null;
        JSONArray jobList=null;
        JSONObject params = null;
        
        try {
            jobID=vals.get("jobID");
            appID=vals.get("appID");
            returnQueue=(String)vals.get("returnQueue");
            try {
                returnQueueTimeout = (Long)vals.get("returnQueueTimeout");
            }
            catch (Exception ex) {
                log.error("errore",ex);
                
            }
            jobList=(JSONArray)vals.get("jobList");

        }
        catch (Exception e) {
            log.error("Unable to get valid data:", e);
            throw new IllegalArgumentException ("Error parsing input job", e);
        }
        JSONArray jres=new JSONArray();
        String jobN = null;
        try {
            for (int i=0;i< jobList.size() ;i++){
                JSONObject j=(JSONObject) jobList.get(i);
                jobN=(String)j.get("jobN");
                String jobType=(String)j.get("jobType");
                String bag = (String)j.get("bag");
                params=(JSONObject)j.get("params");
                //Class<Chef> cClass=(Class<Chef>) Class.forName("it.bologna.ausl.masterchef.workers."+jobType+"Worker",true,cl);
                Class<Chef> cClass=(Class<Chef>) Class.forName("it.bologna.ausl.masterchef.workers." + jobType + "Worker", true, this.getClass().getClassLoader());
                Chef c=cClass.newInstance();
                log.debug("executing job id: " + jobID + " - JobType: " + jobType + "...");
                JSONObject tmpres=c.doWork(params, i, jres, workDir, tmpDir);
                JSONObject r=new JSONObject();
                r.put("jobN", jobN);
                r.put("jobType",jobType);
                r.put("bag",bag);
                r.put("res",tmpres);
                jres.add(r);
            }
        }
        catch (WorkerException ex) {
            ex.setAppID((String) appID);
            ex.setJobID((String) jobID);
            ex.setJobN(jobN);
            log.error("Error executing job", ex);
            log.error("params:\n" + params);
            throw ex;
        }
        catch (Exception | Error e) {
            WorkerException ex = new WorkerException(null, e);
            ex.setAppID((String) appID);
            ex.setJobID((String) jobID);
            ex.setJobN(jobN);
            log.error("Error executing job", e);
            log.error("params:\n" + params);
            throw ex;
        }
        JSONObject res=new JSONObject();
        res.put("jobID", jobID);
        res.put("appID", appID);
        res.put("status","OK");
        res.put("results", jres);

        return res;
    }

    @Override
    public void run() {
        try {
            cl=new URLClassLoader(new URL[]{new File(".").toURI().toURL()});
        }
        catch (MalformedURLException ex) {
            // TODO Auto-generated catch block
            log.error("errore",ex);
        }
        String mess;
        JSONObject res;
        while (!exit){
            try {
                mess=redis.takeString(5);
                if (mess==null) 
                    continue;
                try {
                    res = handleMessage(mess);
                    //ERRORE
                    if (res==null) {
                        res=new JSONObject();
                        res.put("status", "KO");
                    }	
                }
                catch (Exception ex) {
                    log.error("errore",ex);
                    res=new JSONObject();
                    if (ex instanceof WorkerException) {
                        WorkerException workerEx = (WorkerException) ex;
                        res.put("appID", workerEx.getAppID());
                        res.put("jobID", workerEx.getJobID());
                        res.put("jobN", workerEx.getJobN());
                        res.put("jobType", workerEx.getJobType());
                        ObjectMapper mapper = new ObjectMapper();
                        res.put("errorDetails", mapper.writeValueAsString(workerEx.getErrorDetails()));
                    }
                    res.put("status","KO");
                    res.put("error",ex.toString());
                }
                if (returnQueue == null)
                    redis.pushout(res.toJSONString());
                else {
                    redis.push(returnQueue, res.toJSONString());
                    if (returnQueueTimeout != null)
                        redis.expire(returnQueue, returnQueueTimeout.intValue());
                }
                redis.done();
            }
            catch (Exception e) {
                log.error("Error in main loop:",e);
                try {
                    Thread.sleep(5000);
                }
                catch (InterruptedException ex) {
                }
            }
            catch (Error err) {
                log.error("errore",err);
            }
            finally{}
        }
    }
}
