package org.ijsberg.iglu.event.messaging.message;

import org.ijsberg.iglu.event.EventType;
import org.ijsberg.iglu.event.messaging.EventMessage;

public class BasicEventMessage implements EventMessage {
    private EventType eventType;

    public BasicEventMessage(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getType() {
        return eventType;
    }
}
