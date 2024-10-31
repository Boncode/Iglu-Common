package org.ijsberg.iglu.event;

import java.util.*;

public enum IgluEventTopic implements EventTopic {

    IGLU_EVENTS(IgluEventType.values());

    private static Set<String> names = new HashSet<>();
    private Map<String, EventType> eventTypes = new LinkedHashMap<>();

    IgluEventTopic(EventType... eventTypes) {
        for (EventType eventType : eventTypes) {
            this.eventTypes.put(eventType.getId(), eventType);
        }
    }

    @Override
    public Collection<EventType> getEventTypes() {
        return eventTypes.values();
    }

    @Override
    public EventType getEventTypeById(String id) {
        return eventTypes.get(id);
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getLabel() {
        return name().replaceAll("_", " ").toLowerCase();
    }

    public static IgluEventTopic byName(String name) {
        for (IgluEventTopic topic : IgluEventTopic.values()) {
            if (topic.getName().equals(name)) {
                return topic;
            }
        }
        return null;
    }
}