package it.bologna.ausl.masterchef;

/**
 *
 * @author gdm
 */
public class Configuration {
    private static String redisHost;
    private static Integer redisPort;
    private static int redisMaxConnection;
    private static String inQueue;
    private static String workQueue;
    private static String outQueue;
    
    public static String getRedisHost() {
        return redisHost;
    }

    public static void setRedisHost(String redisHost) {
        Configuration.redisHost = redisHost;
    }

    public static Integer getRedisPort() {
        return redisPort;
    }

    public static void setRedisPort(Integer redisPort) {
        Configuration.redisPort = redisPort;
    }

    public static int getRedisMaxConnection() {
        return redisMaxConnection;
    }

    public static void setRedisMaxConnection(int redisMaxConnection) {
        Configuration.redisMaxConnection = redisMaxConnection;
    }

    public static String getInQueue() {
        return inQueue;
    }

    public static void setInQueue(String inQueue) {
        Configuration.inQueue = inQueue;
    }

    public static String getWorkQueue() {
        return workQueue;
    }

    public static void setWorkQueue(String workQueue) {
        Configuration.workQueue = workQueue;
    }

    public static String getOutQueue() {
        return outQueue;
    }

    public static void setOutQueue(String outQueue) {
        Configuration.outQueue = outQueue;
    }

}
