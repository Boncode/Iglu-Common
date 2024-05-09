package org.ijsberg.iglu.event;

import java.util.Collection;

public interface EventTopic {
    Collection<EventType> getEventTypes();

    EventType getEventTypeById(String id);
    String getName();

    String getLabel();
}
