package org.ijsberg.iglu.event;

import org.ijsberg.iglu.event.model.Event;
import org.ijsberg.iglu.event.model.EventTopic;
import org.ijsberg.iglu.event.model.EventType;

import java.util.List;
import java.util.Map;

public interface EventBus {

    void registerEventTopic(EventTopic eventTopic, EventType... eventTypes);

    void subscribe(EventTopic eventTypesToReceive, EventListener listener);

    void unsubscribe(EventTopic eventTypesToReceive, EventListener listener);

    <T extends Event> void publish(T event);

    Map<EventTopic<? extends Event>, List<Event>> getLatestEvents();
}
