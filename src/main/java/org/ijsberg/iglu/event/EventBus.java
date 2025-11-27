package org.ijsberg.iglu.event;

import org.ijsberg.iglu.event.model.Event;
import org.ijsberg.iglu.event.model.EventTopic;
import org.ijsberg.iglu.event.model.EventType;

import java.util.List;
import java.util.Map;

public interface EventBus {

    void registerEventTopic(EventTopic<? extends Event> eventTopic, EventType... eventTypes);

    void subscribe(EventTopic<? extends Event> eventTypesToReceive, EventListener<? extends Event> listener);

    void unsubscribe(EventTopic<? extends Event> eventTypesToReceive, EventListener<? extends Event> listener);

    void unsubscribe(EventListener<? extends Event> listener);

    <T extends Event> void publish(T event);

    Map<EventTopic<? extends Event>, List<Event>> getLatestEvents();

    void subscribeToAll(EventListener<? extends Event> listener);
}
