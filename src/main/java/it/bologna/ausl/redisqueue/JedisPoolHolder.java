package it.bologna.ausl.redisqueue;

import java.util.HashMap;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;



/**
 *
 * @author andrea (zuk)
 */
public class JedisPoolHolder {

    private static volatile HashMap<String, JedisPool> jpm = new HashMap<>(5);

    protected JedisPoolHolder() {
    }

    public static JedisPool getInstance(String host, Integer port, int maxConnection) {
        if (port == null || port == -1) {
            port = 6379;
        }
        String hashKey = host + ":" + port.toString();
        if (jpm.get(hashKey) == null) {
            synchronized (JedisPoolHolder.class) {
                if (jpm.get(hashKey) == null) {
                    JedisPoolConfig jpc = new JedisPoolConfig();
                    jpc.setMaxTotal(maxConnection);
                    JedisPool tmp = new JedisPool(jpc, host, port);
                    jpm.put(hashKey, tmp);
                }
            }
        }
        return jpm.get(hashKey);
    }

}
