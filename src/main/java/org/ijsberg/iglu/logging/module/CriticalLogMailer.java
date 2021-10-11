package org.ijsberg.iglu.logging.module;

import org.ijsberg.iglu.access.AccessManager;
import org.ijsberg.iglu.access.MailMessage;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.logging.Logger;

public class CriticalLogMailer implements Logger {


    private Level level = Level.CRITICAL;
    private AccessManager accessManager;

    public CriticalLogMailer(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Override
    public void log(LogEntry entry) {
        if(entry.getLevel().equals(level)) {
            accessManager.dropMessage("System", new MailMessage("[CRT]", entry.getMessage()));
        }
    }

    @Override
    public void addAppender(Logger appender) {

    }

    @Override
    public void removeAppender(Logger appender) {

    }
}
