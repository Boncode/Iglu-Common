package org.ijsberg.iglu.event.messaging;

import org.ijsberg.iglu.event.EventTopic;
import org.ijsberg.iglu.event.EventType;

import java.time.Instant;

public interface EventMessage {

    EventTopic getTopic();

    EventType getType();

    Instant getTimestampUtc();

    String getLocation();

    String getMessage();
}
