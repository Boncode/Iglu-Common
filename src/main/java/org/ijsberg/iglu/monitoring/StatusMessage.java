package org.ijsberg.iglu.monitoring;

public class StatusMessage {

    private String source;
    private boolean active;
    private String message;

    public StatusMessage(String source, boolean active, String message) {
        this.source = source;
        this.active = active;
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public boolean isActive() {
        return active;
    }

    public String getMessage() {
        return message;
    }
}
