package org.ijsberg.iglu.event.monitoring;

public class MonitorStatusMessage {

    public enum Status {
        OK,
        NOT_OK
    }

    private String source;
    private boolean active;
    private String message;

    private Status status = Status.OK;

    public MonitorStatusMessage(String source, boolean active, String message) {
        this.source = source;
        this.active = active;
        this.message = message;
    }

    public MonitorStatusMessage(String source, boolean active, String message, Status status) {
        this.source = source;
        this.active = active;
        this.message = message;
        this.status = status;
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

    public Status getStatus() {
        return status;
    }
}
