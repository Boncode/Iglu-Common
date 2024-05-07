package org.ijsberg.iglu.event.messaging;

/**
 * A message that contains event information or text information that can be consumed by a user
 */
public interface UserConsumableMessage {

    String getMessageText();

    MessageStatus getMessageStatus();
}
