package org.ijsberg.iglu.logging.module;

import org.ijsberg.iglu.access.AccessManager;
import org.ijsberg.iglu.messaging.module.MailMessage;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.logging.Logger;

import java.util.Properties;

public class CriticalLogMailer implements Logger {

    private Level level = Level.CRITICAL;
    private AccessManager accessManager;

    public CriticalLogMailer(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Override
    public void log(LogEntry entry) {
        if(entry.getLevel().equals(level)) {
            if(entry.getData() == null || !entry.getData().getClass().getSimpleName().equals("MessagingException")) {
                accessManager.dropMessage("System", new MailMessage("[CRT]", entry.getMessage()
                        + "\n\n" + getStackTraceFromLogEntry(entry)
                ));
            }
        }
    }

    private String getStackTraceFromLogEntry(LogEntry entry) {
        if (entry.getData() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(entry.getData());
            if (entry.getData() instanceof Throwable) {
                Throwable t = (Throwable) entry.getData();
                sb.append("\n");
                for (int i = 0; i < t.getStackTrace().length; i++) {
                    sb.append("at " + t.getStackTrace()[i]);
                    sb.append("\n");
                }
                Throwable cause = ((Throwable) entry.getData()).getCause();
                while (cause != null) {
                    sb.append("\n");
                    sb.append("caused by:");
                    sb.append(cause);
                    cause = cause.getCause();
                }
            }
            return sb.toString();
        }
        return "No stacktrace provided";
    }

    @Override
    public void addAppender(Logger appender) {

    }

    @Override
    public void removeAppender(Logger appender) {

    }

    @Override
    public int getLogLevelOrdinal() {
        return level.ordinal();
    }

    @Override
    public Properties getProperties() {
        return null;
    }
}
