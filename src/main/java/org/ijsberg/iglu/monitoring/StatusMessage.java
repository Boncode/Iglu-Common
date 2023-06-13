package org.ijsberg.iglu.monitoring;

public class StatusMessage {

    public enum Status {
        OK,
        NOT_OK
    }

    private String source;
    private boolean active;
    private String message;

    private Status status = Status.OK;

    public StatusMessage(String source, boolean active, String message) {
        this.source = source;
        this.active = active;
        this.message = message;
    }

    public StatusMessage(String source, boolean active, String message, Status status) {
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
