package org.ijsberg.iglu.access;

public class UserMessage implements UserConsumableMessage {

    private String messageText;

    public UserMessage(String messageText) {
        this.messageText = messageText;
    }


    @Override
    public String getMessageText() {
        return messageText;
    }

    public String toString() {
        return messageText;
    }
}
