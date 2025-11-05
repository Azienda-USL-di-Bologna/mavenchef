package it.bologna.ausl.masterchef;

import it.bologna.ausl.masterchef.errors.WorkerException;
import java.io.File;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * 
 * @author gdm
 */
public interface Chef {
    /**
     * 
     * @param in
     * @param i
     * @param otherin
     * @param workDir
     * @param tmpDir
     * @return
     * @throws WorkerException 
     */
    public JSONObject doWork(JSONObject in, int i, JSONArray otherin, File workDir, File tmpDir) throws WorkerException;
    public String getJobType();
}
