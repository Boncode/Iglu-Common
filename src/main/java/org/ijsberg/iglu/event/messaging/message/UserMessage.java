package org.ijsberg.iglu.event.messaging.message;

import org.ijsberg.iglu.event.messaging.MessageStatus;
import org.ijsberg.iglu.event.messaging.UserConsumableMessage;

public class UserMessage implements UserConsumableMessage {

    private final String messageText;
    private final MessageStatus messageStatus;

    public UserMessage(String messageText) {
        this.messageText = messageText;
        this.messageStatus = MessageStatus.INFO;
    }

    public UserMessage(String messageText, MessageStatus messageStatus) {
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

    public String toString() {
        return messageText;
    }
}
