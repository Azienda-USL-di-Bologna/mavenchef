package it.bologna.ausl.redisqueue;

import it.bologna.ausl.redis.RedisClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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

    RedisClient redisClient;
    private final String inq, outq, wq;
    private byte[] curObj = null;
    private static final Logger log = LogManager.getLogger(RedisQueueManager.class);

    /**
     * costruisce l'oggetto RedisQueueManager
     * @param redisHost host di redis
     * @param redisPort porta di redis (default 6379)
     * @param redisDB db di redis da usare
     * @param maxConnection numero massimo di connessioni
     * @param inQueueName nome della coda di input
     * @param outQueueName nome della coda di output di default
     * @param workingQueueName nome della coda di work
     * @param password la password epr la connessione a redis (null se non è impostata)
     */
    public RedisQueueManager(String redisHost, Integer redisPort, int redisDB, int maxConnection, String inQueueName, String outQueueName, String workingQueueName, String password) {
        this.inq = inQueueName;
        this.outq = outQueueName;
        this.wq = workingQueueName;
        this.redisClient = new RedisClient(redisHost, redisPort, redisDB, password, outq, inq);
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
            if (payload != null) {
                return redisClient.put(payload, queue);
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new RedisQueueManagerException("error pushing", e);
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
            return redisClient.set(key, value);
        } catch (Exception e) {
            throw new RedisQueueManagerException("error setting", e);
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
            return redisClient.getKey(key);
        } catch (Exception e) {
            throw new RedisQueueManagerException("error getting", e);
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
            return redisClient.del(key);
        } catch (Exception e) {
            throw new RedisQueueManagerException("error deleting", e);
        }
    }

    /**
     * imposta la scadenza di una chiave
     * @param key 
     * @param seconds
     * @return "true" se la scadenza è stata impostata correttamente
     * @throws RedisQueueManagerException 
     */
    public boolean expire(String key, long seconds) throws RedisQueueManagerException {
        try {
            return redisClient.expire(key, seconds) > 0;
        } catch (Exception e) {
            throw new RedisQueueManagerException("error setting expire", e);
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
            String res = redisClient.brpoplpush(inq, wq, timeout);
            if (res != null) {
                curObj = res.getBytes();
            } else {
                res = null;
            }
            return res;
        } catch (Exception e) {
            throw new RedisQueueManagerException("error taking", e);
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
            redisClient.lrem(wq.getBytes(), curObj);
            curObj = null;
            return true;
        } catch (Exception e) {
            throw new RedisQueueManagerException("error doning", e);
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
            String res = redisClient.bpop(queue, timeout);
            return res;
        } catch (Exception e) {
            throw new RedisQueueManagerException("error bpopping", e);
        }
    }
}
