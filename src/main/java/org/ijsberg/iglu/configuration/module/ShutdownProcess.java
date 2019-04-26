package org.ijsberg.iglu.configuration.module;

import org.ijsberg.iglu.Application;
import org.ijsberg.iglu.logging.LogEntry;

/**
 * Performs a (forced) shutdown sequence.
 */
public class ShutdownProcess implements Runnable {

    private Application application;

    public ShutdownProcess(Application application) {
        this.application = application;
    }

    /**
     * Invokes shutdown when startup is completed.
     */
    public void run() {
        System.out.println(new LogEntry("starting" + (application.isRunning() ? " forced" : "")
                + " application shutdown process..."));
        if (application.isStarted()) {
            try {
                application.stop();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        System.out.println(new LogEntry("Application shutdown process completed..."));
    }
}
