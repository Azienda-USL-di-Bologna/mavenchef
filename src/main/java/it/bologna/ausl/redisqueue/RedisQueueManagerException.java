package it.bologna.ausl.redisqueue;

public class RedisQueueManagerException   extends Exception {

	private static final long serialVersionUID = 1L;
	public RedisQueueManagerException (String message){
		super(message);
	}

	public RedisQueueManagerException(String message, Throwable cause) {
		super (message,cause);
	}
}
