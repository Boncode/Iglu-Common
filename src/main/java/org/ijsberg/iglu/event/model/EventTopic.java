package org.ijsberg.iglu.event.model;

public record EventTopic<T extends Event>(
    String id,
    String label,
    String description,
    Class<T> eventClass) {
}
