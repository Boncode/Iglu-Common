package org.ijsberg.iglu.event.model;

import java.time.Instant;

public abstract class BasicEvent implements Event {

    private final EventType type;
    private final Instant timestampUtc;
    private final String assetId;

    public BasicEvent(EventType type, Instant timestampUtc, String assetId) {
        this.type = type;
        this.timestampUtc = timestampUtc;
        this.assetId = assetId;
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
    public String getAssetId() {
        return assetId;
    }

    public String getMessage() {
        return toString();
    }

    public String getDescription() {
        return type.getLabel();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {" +
                "type=" + type +
                ", timestampUtc=" + timestampUtc +
                '}';
    }

    protected static String getEventTypeLabel(EventType eventType) {
        return getEventTypeLabel(eventType.getId());
    };

    public static String getEventTypeLabel(String eventType) {
        return eventType.replaceAll("_", " ").toLowerCase();
    };

}
