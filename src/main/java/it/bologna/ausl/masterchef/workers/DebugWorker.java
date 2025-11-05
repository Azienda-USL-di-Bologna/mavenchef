package it.bologna.ausl.masterchef.workers;

import it.bologna.ausl.masterchef.Chef;
import java.io.File;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DebugWorker implements Chef{
private static final Logger log = LogManager.getLogger(DebugWorker.class);
	@Override
	public JSONObject doWork(JSONObject in, int i, JSONArray otherin, File workDir, File tmpDir) {
		log.info("IN: "+in);
		log.info("i : "+i);
		log.info("otherIn: "+otherin);
		return in;	
	}

	
	
	@Override
	public final String getJobType() {
		return "Debug";
	}

}
