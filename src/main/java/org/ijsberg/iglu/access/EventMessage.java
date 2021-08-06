package org.ijsberg.iglu.access;

public class EventMessage implements UserConsumableMessage {

    private String type;
    private String messageText;

    public EventMessage(String type, String messageText) {
        this.type = type;
        this.messageText = messageText;
    }

    @Override
    public String getMessageText() {
        return messageText;
    }

    public String getType() {
        return type;
    }
}
