package it.bologna.ausl.masterchef;

import it.bologna.ausl.masterchef.workers.Worker;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class Shutdown extends Thread {
private static final Logger log = LogManager.getLogger(Shutdown.class);
    private final Worker[] workers;

    public Shutdown(Worker[] workers) {
        this.workers = workers;
        setName("Shutdown worker");
    }

    @Override
    public void run() {
        System.out.println("Shutdown initiated");
        System.out.flush();

        int i = 0;
        for (Worker jw : workers) {
            if (jw.isAlive()) {
                jw.exit();
            } else {
                workers[i] = null;
            }
            i++;
        }
        for (Worker jw : workers) {
            if (jw == null) {
                continue;
            }
            try {
                jw.join(10000);
            } 
            catch (InterruptedException e) {
                log.error(e);
            }
        }
        System.out.println("All worker threads are gone");
        System.out.flush();
    }
}
