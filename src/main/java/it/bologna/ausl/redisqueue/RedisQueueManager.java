package it.bologna.ausl.redisqueue;

import java.util.List;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Questa classe implementa una astrazione a code su Redis.
 * 
 * Gli oggetti vengono atomicamente trasferiti dalla coda di input alla coda di work attraverso la funzione takeString o takeBytes che a sua vuolta utilizza la primitiva blpoprpush
 * 
 * Ad ogni take il valore dell'oggetto viene traferito in una variabile accedibile tramite il metodo getCurObj().
 * 
 * Dopo il primo take, per poterne effettuare un altro è necessario chiamare il metodo done().
 * 
 * @author gdm
 */
public class RedisQueueManager {

    private Jedis c;
    private final JedisPool jp;
    private final String inq, outq, wq;
    private byte[] curObj = null;
    private static final Logger log = LogManager.getLogger(RedisQueueManager.class);

    /**
     * costruisce l'oggetto RedisQueueManager
     * @param redisHost host di redis
     * @param redisPort porta di redis (default 6379)
     * @param maxConnection numero massimo di connessioni
     * @param inQueueName nome della coda di input
     * @param outQueueName nome della coda di output di default
     * @param workingQueueName nome della coda di work
     */
    public RedisQueueManager(String redisHost, Integer redisPort, int maxConnection, String inQueueName, String outQueueName, String workingQueueName) {
        jp = JedisPoolHolder.getInstance(redisHost, redisPort, maxConnection);
        this.inq = inQueueName;
        this.outq = outQueueName;
        this.wq = workingQueueName;
    }

    /**
     * torna l'ultimo oggetto letto tramite una takeString o takeBytes
     * @return 
     */
    public byte[] getCurObj() {
        return curObj;
    }

    /**
     * accoda nella lista di input il valore passato
     * @param payload il valore da accodare
     * @return "true" se l'accodamento è andato a buon fine
     * @throws RedisQueueManagerException 
     */
    public boolean pushin(String payload) throws RedisQueueManagerException {
        return push(inq, payload);
    }

    /**
     * accoda nella lista di output il valore passato
     * @param payload il valore da accodare
     * @return "true" se l'accodamento è andato a buon fine
     * @throws RedisQueueManagerException 
     */
    public boolean pushout(String payload) throws RedisQueueManagerException {
        return push(outq, payload);
    }

    /**
     * accoda nella lista passata il valore passato
     * @param queue la lista
     * @param payload il valore da accodare
     * @return "true" se l'accodamento è andato a buon fine
     * @throws RedisQueueManagerException 
     */
    public boolean push(String queue, String payload) throws RedisQueueManagerException {
        try {
            c = jp.getResource();
            if (payload == null) {
                return false;
            }
            return (c.lpush(queue, payload) > 0);
        } catch (Exception e) {
            jp.returnBrokenResource(c);
            c = null;
            throw new RedisQueueManagerException("error pushing", e);
        } finally {
            if (c != null) {
                jp.returnResource(c);
            }
        }
    }
    
    /**
     * accoda nella lista di input il valore in bytes passato
     * @param payload il valore in bytes da accodare
     * @return "true" se l'accodamento è andato a buon fine
     * @throws RedisQueueManagerException 
     */
    public boolean pushin(byte[] payload) throws RedisQueueManagerException {
        return push(inq, payload);
    }
    /**
     * accoda nella lista di output il valore in bytes passato
     * @param payload il valore da in bytes accodare
     * @return "true" se l'accodamento è andato a buon fine
     * @throws RedisQueueManagerException 
     */
    public boolean pushout(byte[] payload) throws RedisQueueManagerException {
        return push(outq, payload);
    }
    /**
     * accoda nella lista passata il valore in bytes passato
     * @param queue la lista
     * @param payload il valore da in bytes accodare
     * @return "true" se l'accodamento è andato a buon fine
     * @throws RedisQueueManagerException 
     */
    public boolean push(String queue, byte[] payload) throws RedisQueueManagerException {
        try {
            c = jp.getResource();
            if (payload == null) {
                return false;
            }
            return (c.lpush(queue.getBytes(), payload) > 0);
        } catch (Exception e) {
            jp.returnBrokenResource(c);
            c = null;
            throw new RedisQueueManagerException("error pushing", e);
        } finally {
            if (c != null) {
                jp.returnResource(c);
            }
        }
    }

    /**
     * crea/setta il valore nella chiave passata
     * @param key
     * @param value
     * @return "OK" se tutto ok
     * @throws RedisQueueManagerException 
     */
    public String set(String key, String value) throws RedisQueueManagerException {
        try {
            c = jp.getResource();
            return (c.set(key, value));
        } catch (Exception e) {
            jp.returnBrokenResource(c);
            c = null;
            throw new RedisQueueManagerException("error setting", e);
        } finally {
            if (c != null) {
                jp.returnResource(c);
            }
        }
    }

    /**
     * torna il valore identificato dalla chiave
     * @param key
     * @return il valore identificato dalla chiave
     * @throws RedisQueueManagerException 
     */
    public String get(String key) throws RedisQueueManagerException {
        try {
            c = jp.getResource();
            return c.get(key);
        } catch (Exception e) {
            jp.returnBrokenResource(c);
            c = null;
            throw new RedisQueueManagerException("error getting", e);
        } finally {
            if (c != null) {
                jp.returnResource(c);
            }
        }
    }

    /**
     * elimina la chiave passata
     * @param key
     * @return il numero di chiavi eliminate (dovrebbe essere sempre 1 oppure 0)
     * @throws RedisQueueManagerException 
     */
    public Long del(String key) throws RedisQueueManagerException {
        try {
            c = jp.getResource();
            return (c.del(key));
        } catch (Exception e) {
            jp.returnBrokenResource(c);
            c = null;
            throw new RedisQueueManagerException("error deleting", e);
        } finally {
            if (c != null) {
                jp.returnResource(c);
            }
        }
    }

    /**
     * imposta la scadenza di una chiave
     * @param key 
     * @param seconds
     * @return "true" se la scadenza è stata impostata correttamente
     * @throws RedisQueueManagerException 
     */
    public boolean expire(String key, int seconds) throws RedisQueueManagerException {
        try {
            c = jp.getResource();
            return (c.expire(key, seconds) > 0);
        } catch (Exception e) {
            jp.returnBrokenResource(c);
            c = null;
            throw new RedisQueueManagerException("error setting expire", e);
        } finally {
            if (c != null) {
                jp.returnResource(c);
            }
        }
    }

    /**
     * sposta atomicamente nella coda di work il valore in testa nella coda di input.
     * se la coda di input è vuota aspetta fino a "timeout" tempo e poi torna null
     * @param timeout
     * @return la stringa letta, oppure null se scade il timeout
     * @throws RedisQueueManagerException 
     */
    public String takeString(int timeout) throws RedisQueueManagerException {
        if (curObj != null) {
            throw new RedisQueueManagerException("curObj is not null!");
        }
        try {
            c = jp.getResource();
            String res = c.brpoplpush(inq, wq, timeout);
            if (res == null) {
                return null;
            }
            curObj = res.getBytes();
            return res;
        } catch (Exception e) {
            jp.returnBrokenResource(c);
            c = null;
            throw new RedisQueueManagerException("error taking", e);
        } finally {
            if (c != null) {
                jp.returnResource(c);
            }
        }
    }

    /**
     * sposta atomicamente nella coda di work il valore in testa nella coda di input.
     * Se la coda di input è vuota aspetta fino a "timeout" tempo e poi torna null
     * @param timeout
     * @return il vaore in bytes letto, oppure null se scade il timeout
     * @throws RedisQueueManagerException 
     */
    public byte[] takeBytes(int timeout) throws RedisQueueManagerException {
        if (curObj != null) {
            throw new RedisQueueManagerException("curObj is not null!");
        }
        try {
            c = jp.getResource();
            curObj = c.brpoplpush(inq.getBytes(), wq.getBytes(), timeout);
            return curObj;
        } catch (Exception e) {
            jp.returnBrokenResource(c);
            c = null;
            throw new RedisQueueManagerException("error taking", e);
        } finally {
            if (c != null) {
                jp.returnResource(c);
            }
        }
    }

    /**
     * termina la lettura e prepara l'ogetto per la prossima
     * @return "true" se tutto ok
     * @throws RedisQueueManagerException 
     */
    public boolean done() throws RedisQueueManagerException {
        if (curObj == null) {
            return true;
        }
        try {
            c = jp.getResource();
            c.lrem(wq.getBytes(), 0, curObj);
            curObj = null;
            return true;
        } catch (Exception e) {
            jp.returnBrokenResource(c);
            c = null;
            throw new RedisQueueManagerException("error doning", e);
        } finally {
            if (c != null) {
                jp.returnResource(c);
            }
        }
    }

    /**
     * estrae un valore dalla coda e lo ritorna.
     * Se la coda di input è vuota aspetta fino a "timeout" tempo e poi torna null
     * @param queue
     * @param timeout
     * @return il valore estratto
     * @throws RedisQueueManagerException 
     */
    public String bpop(String queue, int timeout) throws RedisQueueManagerException {
        try {
            c = jp.getResource();
            List<String> obj = c.brpop(timeout, queue);
            if (obj == null) {
                return null;
            }
            String res = obj.get(1);
            return res;
        } catch (Exception e) {
            jp.returnBrokenResource(c);
            c = null;
            throw new RedisQueueManagerException("error bpopping", e);
        } finally {
            if (c != null) {
                jp.returnResource(c);
            }
        }
    }
}
