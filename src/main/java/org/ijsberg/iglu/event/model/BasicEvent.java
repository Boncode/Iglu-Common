package org.ijsberg.iglu.event.model;

import java.time.Instant;

public class BasicEvent implements Event {

    private final EventType type;
    private final Instant timestampUtc;

    public BasicEvent(EventType type, Instant timestampUtc) {
        this.type = type;
        this.timestampUtc = timestampUtc;
    }

    @Override
    public EventType getType() {
        return type;
    }

    @Override
    public Instant getTimestampUtc() {
        return timestampUtc;
    }

    @Override
    public String toString() {
        return "BasicEvent{" +
                "type=" + type +
                ", timestampUtc=" + timestampUtc +
                '}';
    }
}
