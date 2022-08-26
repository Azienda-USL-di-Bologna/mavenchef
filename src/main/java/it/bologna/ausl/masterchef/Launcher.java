package it.bologna.ausl.masterchef;

import it.bologna.ausl.masterchef.workers.Worker;
import it.bologna.ausl.parameters_client.ParametersClient;
import it.bologna.ausl.parameters_client.ParametersDbClient;
import it.bologna.ausl.redisqueue.RedisQueueManager;
import it.bologna.ausl.redisqueue.RedisQueueManagerException;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class Launcher {
    private static Logger log = LogManager.getLogger(Launcher.class);
    static {
        File localLog4jConfigurationFile = new File("src/main/resources/log4j2.xml");
        if (localLog4jConfigurationFile.exists()) {
            Configurator.initialize(null, localLog4jConfigurationFile.getAbsolutePath());
        }
//        Configurator.initialize(null, "src/main/resources/log4j2.xml");
//        System.setProperty("log4j.configurationFile", "file://F:\\Progetti\\ProgettiJava\\NT\\mavenchef\\src\\main\\resources\\log4j2.xml");
//    System.out.println("log4j.configurationFile: " + System.getProperty("log4j.configurationFile", "niente"));
//    log = LogManager.getLogger(Launcher.class);
//    log.info("matcher2.fin");
    }
//	private static final String configFile=System.getProperty("masterChef.configFile","masterchef.properties");
    public static Map<String, String> emlReplaceMap;
    public static Properties config = loadConfig();
//    public static Properties config = new Properties();
    private static final String MASTERCHEF_CONFIG_PARAM_NAME = "masterChefConfig";

    public static Properties loadConfig() {
        try {
            ParametersClient parametersClient = new ParametersDbClient();
            String regex = "\\&\\{.*::json\\}";
            String regexGetNomeParametro = "\\&\\{([^\\}]+)::json}";
            Pattern pattern = Pattern.compile(regex);
            Pattern patternGetNomeParametro = Pattern.compile(regexGetNomeParametro);
            config = new Properties();
            config.load(new StringReader(parametersClient.getParameter(MASTERCHEF_CONFIG_PARAM_NAME)));
            config.keySet().stream().forEach(key -> {
                String value = (String) config.get(key);
                log.info(key.toString() + " -> " + value);
                Matcher matcher = pattern.matcher(value);
                if (matcher.find()) {
                    Matcher matcherGetNomeParametro = patternGetNomeParametro.matcher(value);
                    log.info("matcher2.find(): " + matcherGetNomeParametro.find());
                    log.info("matcher2.group(0): " + matcherGetNomeParametro.group(0));
                    log.info("matcher2.group(1): " + matcherGetNomeParametro.group(1));
                    try {
                        config.setProperty(key.toString(), value.replaceAll(regex, parametersClient.getParameter(matcherGetNomeParametro.group(1))));
                    } catch (IOException ex) {
                        ex.printStackTrace(System.out);
                        log.error("errore nel replace del json della proprieta " + key);
                    }
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            log.fatal("errore nella lettura dei parametri", ex);
        }

        ///////////////PER PROVE LOCALI
//            config.put("ioda.document.update", "http://localhost:8082/bds_tools/ioda/api/document/update");
//            config.put("masterchef.concurrency", "1");
//            config.put("masterchef.workdir", "workdir");
//            config.put("masterchef.tmpdir", "workdir/tmp");
//            config.put("redis.host", "localhost");
        //////////////
        return config;
    }

    /**
     * @param args
     * @throws RedisQueueManagerException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws RedisQueueManagerException, InterruptedException {
        log.info("charset: " + System.getProperty("file.encoding"));
//            BasicConfigurator.configure();
//            log.setLevel(Level.DEBUG);
//            File f = new File("file://src/main/resources/log4j2.xml");
//            System.setProperty("log4j.configurationFile", "file://F:\\Progetti\\ProgettiJava\\NT\\mavenchef\\src\\main\\resources\\log4j2.xml");
//            LoggerContext context = (LoggerContext) LogManager.getContext(false);
//            context.setConfigLocation(f.toURI());

//            Logger log2 = LogManager.getLogger(Launcher.class);
//            log.info("log4j.configurationFile: " + System.getProperty("log4j.configurationFile", "niente"));
//            System.out.println("log4j.configurationFile: " + System.getProperty("log4j.configurationFile", "niente"));
//            Properties config=loadConfig(configFile);
//            JedisPoolConfig jpc;
//	    JedisPool jp;
//            String host=config.getProperty("redis.host","babelcorso1");
// if (config == null){System.out.println("config è NULL");}else{System.out.println(config.toString());}
        String redisHost = config.getProperty("redis.host", "localhost");
        Integer port = null;
        if (config.getProperty("redis.port") != null && !config.getProperty("redis.port").equals("")) {
            port = Integer.valueOf(config.getProperty("redis.port"));
        }
        int nthreads = Integer.valueOf(config.getProperty("masterchef.concurrency", "3"));
        String qin = config.getProperty("redis.inQueue", "chefingdml");
        String qout = config.getProperty("redis.outQueue", "chefoutgdml");
        String qwork = config.getProperty("redis.workQueue", "chefworkgdml");

        Configuration.setRedisHost(redisHost);
        Configuration.setRedisPort(port);
        Configuration.setRedisMaxConnection(nthreads);
        Configuration.setInQueue(qin);
        Configuration.setWorkQueue(qwork);
        Configuration.setOutQueue(qout);
        emlReplaceMap = connectAndGetRegex();
        // --- babelcorso
//            String qin=config.getProperty("redis.inQueue","chefincorso");
//            String qout=config.getProperty("redis.outQueue","chefoutcorso");
//            String qwork=config.getProperty("redis.workQueue","chefworkcorso");
        // --------------
//            jpc=new JedisPoolConfig();
//            jpc.setMaxActive(nthreads+1);
//            jpc.setTestOnBorrow(true);
//            jp=new JedisPool(jpc,host);
//            RedisQueueManager redis = new RedisQueueManager(redisHost, port, nthreads + 1, qin, qout, qwork);
        Worker[] workers = new Worker[nthreads];
        int i = 0;
        for (Worker jw : workers) {

            try {
//                    jw=new Worker(config.getProperty("masterchef.workdir",System.getProperty("java.io.tmpdir")), config.getProperty("masterchef.tmpdir",System.getProperty("java.io.tmpdir")),new RedisQueue(jp,qin,qout,qwork));
                jw = new Worker(config.getProperty("masterchef.workdir", System.getProperty("java.io.tmpdir")), config.getProperty("masterchef.tmpdir", System.getProperty("java.io.tmpdir")), new RedisQueueManager(redisHost, port, nthreads + 1, qin, qout, qwork));
            } catch (Exception | Error e) {
                log.fatal(e);
                System.exit(1);
            }
            workers[i] = jw;
            jw.start();
            i++;
        }
        Shutdown s = new Shutdown(workers);
        Runtime.getRuntime().addShutdownHook(s);
    }

    public static Map<String, String> connectAndGetRegex() {
        String executeSqlUrl = config.getProperty("database.url");
        Map<String, String> tmp = new HashMap();
        try (Connection dbConn = DriverManager.getConnection(executeSqlUrl);) {
            String sAllRegex = "select regex, replacement from bds_tools.eml_replace";
            try (PreparedStatement ps = dbConn.prepareStatement(sAllRegex)) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    tmp.put(rs.getString("regex"), rs.getString("replacement"));
                }
                return tmp;
            }

        } catch (SQLException ex) {
            log.error("errore in connectAndGetRegex", ex);
            return null;
        }
    }
}
