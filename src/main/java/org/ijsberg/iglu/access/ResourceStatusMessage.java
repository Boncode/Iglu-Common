package org.ijsberg.iglu.access;

public class ResourceStatusMessage {

    private String status;
    private String message;

    public ResourceStatusMessage(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
