package org.ijsberg.iglu.event;

import java.util.Set;

public interface EventTopic {
    Set<EventType> getEventTypes();

    String getName();

    String getLabel();
}
