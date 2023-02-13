package org.ijsberg.iglu.logging.module;

import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.logging.Logger;
import org.ijsberg.iglu.util.ResourceException;
import org.ijsberg.iglu.util.io.FileSupport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

public class BasicFileLogger implements Logger {

    private final String fileName;

    protected Object lock = new Object();
    private PrintStream logFilePrintStream;


    public BasicFileLogger(String fileName) {
       this.fileName = fileName;
       openLogStream();
    }

    protected void openLogStream() {
        try {
            File file = new File(fileName + ".log");
            if(file.exists() && file.canWrite()) {
                logFilePrintStream = new PrintStream(new FileOutputStream(file, true));
            } else {
                logFilePrintStream = new PrintStream(new FileOutputStream(FileSupport.createFile(fileName + ".log")));
            }
        }
        catch (IOException e) {
            throw new ResourceException("unable to open new logfile '" + fileName + ".log'", e);
        }
    }

    @Override
    public void log(LogEntry entry) {
        synchronized (lock) {
            writeEntry(entry);
        }
    }

    public void writeEntry(LogEntry entry) {
        logFilePrintStream.println(entry);
    }

    @Override
    public void addAppender(Logger appender) {

    }

    @Override
    public void removeAppender(Logger appender) {

    }

    @Override
    public int getLogLevelOrdinal() {
        return 0;
    }

    @Override
    public Properties getProperties() {
        return null;
    }
}
