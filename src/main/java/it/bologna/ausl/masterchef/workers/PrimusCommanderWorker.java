package it.bologna.ausl.masterchef.workers;

import it.bologna.ausl.masterchef.Chef;
import it.bologna.ausl.masterchef.Configuration;
import static it.bologna.ausl.masterchef.Launcher.config;
import it.bologna.ausl.masterchef.errors.WorkerException;
import it.bologna.ausl.redisqueue.RedisQueueManager;
import java.io.File;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 INPUT
 PrimusCommanderJob={
    'appID':"rediscli",
    'jobID':"1",
    'jobList':[ {
        'jobN':"job1",
        'jobType':"PrimusCommander",
        'params': {
            "command": {
                "id":"id del comando"
                "dest":["username"],
                "dest_app":"babel",
                "command": {
                    "command":"showMessage",
                    "params":{"foo":"bar"}
                }
            }
            "interval":"2" secondi
            "times": "1" // passare "0" per infinito
        }
    }],
    'returnQueue':"queueName"
 }

 OUTPUT
 {
 }

 */
public class PrimusCommanderWorker implements Chef {

//    private static final String configFile = System.getProperty("ChefWorker.configFile", "chefWorker.properties");
    private static final Logger log = LogManager.getLogger(PrimusCommanderWorker.class);
    public PrimusCommanderWorker() throws WorkerException {

    }

    @Override
    public JSONObject doWork(JSONObject in, int j, JSONArray otherin, File workDir, File tmpDir) throws WorkerException {
        try {
//            Properties config = Launcher.loadConfig(configFile);
            String primusCommRedisUrl = config.getProperty("primuscommander.redis.url", "babelcorso1");
            Integer primusCommRedisPort = null;
            if (config.getProperty("primuscommander.redis.port")!= null && !config.getProperty("primuscommander.redis.port").equals("")) 
                primusCommRedisPort=Integer.valueOf(config.getProperty("redis.port"));     
//            int primusCommRedisMaxConn = Integer.valueOf(config.getProperty("primuscommander.redis.maxconnection", "4"));
            String primusCommRedisCommandQueue = config.getProperty("primuscommander.redis.commandqueue", "ClientCommands");
            String primusStopString = config.getProperty("primuscommander.stopstring", "stopByGdm");

            String primusCommRedisExecutingKeySuffix = config.getProperty("primuscommander.executingkeysuffix", "_primus_executing_gdml_primus_executing_gdml");
            
            int interval = Integer.valueOf((String) in.get("interval"));
            int times = Integer.valueOf((String) in.get("times"));
            JSONObject command = (JSONObject) in.get("command");

            // per retrocompatibilità potrebbe non esserci
            String commandId = null;
            try {
                commandId = (String) in.get("id");
            }
            catch (Exception ex) {
            }
            
            RedisQueueManager redisPrimus = new RedisQueueManager(primusCommRedisUrl, primusCommRedisPort, Configuration.getRedisMaxConnection(), primusCommRedisCommandQueue, null, null);
            RedisQueueManager redis = new RedisQueueManager(Configuration.getRedisHost(), Configuration.getRedisPort(), Configuration.getRedisMaxConnection(), Configuration.getInQueue(), Configuration.getWorkQueue(), Configuration.getOutQueue());
            log.info("Launching executer thread...");
            PrimusCommandExecuter primusCommandExecuter = new PrimusCommandExecuter(
                    redisPrimus,
                    command,
                    primusCommRedisCommandQueue,
                    primusCommRedisExecutingKeySuffix,
                    commandId,
                    times,
                    interval,
                    redis,
                    primusStopString);

            new Thread(primusCommandExecuter).start();

            JSONObject res = new JSONObject();
            return res;
        }
        catch (Exception ex) {
            throw new WorkerException(getJobType(), ex);
        }
    }

    @Override
    public final String getJobType() {
        return "PrimusCommander";
    }

    private class PrimusCommandExecuter implements Runnable {

        private final RedisQueueManager redisPrimus;
        private final JSONObject command;
        private final String commandQueue;
        private final String executingKeySuffix;
        private final String commandId;
        private final int times;
        private final int interval;

        private final RedisQueueManager redis;
        private final String primusStopString;

        public PrimusCommandExecuter(
                RedisQueueManager redisPrimus,
                JSONObject command,
                String commandQueue,
                String executingKeySuffix,
                String commandId,
                int times,
                int interval,
                RedisQueueManager redis,
                String primusStopString) {

            this.redisPrimus = redisPrimus;
            this.command = command;
            this.commandQueue = commandQueue;
            this.executingKeySuffix = executingKeySuffix;
            
            // se non mi è stato passato il command id ne genero uno io
            if (commandId != null && !commandId.equals(""))
                this.commandId = commandId;
            else
                this.commandId = Long.toString(System.currentTimeMillis());
            this.times = times;
            this.interval = interval;

            this.redis = redis;
            this.primusStopString = primusStopString;
        }

        @Override
        public void run() {
            try {
                boolean stop;
                boolean infiniteRepeat = this.times == 0;
                int timesToRepeatCommand = this.times;
                String executionKey = commandId + executingKeySuffix;
                log.info("setting executionKey: " + executionKey + "...");
                redis.set(executionKey, command.toJSONString());

                do {
                    // leggo dalla coda di stop se mi devo fermare
//                    String stopString = redis.bpop(primusStopQueue, interval > 0 ? interval : 1);
                    
                    // se non trovo più la chiave o il valore è cambiato mi fermo
                    String value = redis.get(executionKey);
                    stop = (value == null) || !command.toJSONString().equals(value);
                    if (!stop) {
                        log.info("Launching command: " + command.toJSONString() + "...");
                        redisPrimus.push(commandQueue, command.toJSONString());
                        if (!infiniteRepeat) {
                            timesToRepeatCommand--;
                        }
                        Thread.sleep(interval > 0 ? interval * 1000 : 1000);
                    }
                }
                while (!stop && (infiniteRepeat || timesToRepeatCommand > 0));

                log.info("Exiting thread...");
                log.info("deleting executionKey: " + executionKey + "...");
                redis.del(executionKey);
            }
            catch (Exception ex) {
                log.error("Error executing command: " + command, ex);
            }
        }

    }
}
