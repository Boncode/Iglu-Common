package org.ijsberg.iglu.event.messaging.message;

import org.ijsberg.iglu.event.messaging.UserConsumableMessage;
import org.ijsberg.iglu.event.messaging.MessageStatus;

public class StatusMessage implements UserConsumableMessage {

    private final String type;
    private final String messageText;
    private final MessageStatus messageStatus;

    public StatusMessage(String type, String messageText) {
        this.type = type;
        this.messageText = messageText;
        this.messageStatus = MessageStatus.INFO;
    }

    public StatusMessage(String type, String messageText, MessageStatus messageStatus) {
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
