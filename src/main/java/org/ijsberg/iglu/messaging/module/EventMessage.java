package org.ijsberg.iglu.messaging.module;

import org.ijsberg.iglu.messaging.MessageStatus;
import org.ijsberg.iglu.messaging.UserConsumableMessage;

public class EventMessage implements UserConsumableMessage {

    private final String type;
    private final String messageText;
    private final MessageStatus messageStatus;

    public EventMessage(String type, String messageText) {
        this.type = type;
        this.messageText = messageText;
        this.messageStatus = MessageStatus.INFO;
    }

    public EventMessage(String type, String messageText, MessageStatus messageStatus) {
        this.type = type;
        this.messageText = messageText;
        this.messageStatus = messageStatus;
    }

    @Override
    public String getMessageText() {
        return messageText;
    }

    @Override
    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public String getType() {
        return type;
    }
}
