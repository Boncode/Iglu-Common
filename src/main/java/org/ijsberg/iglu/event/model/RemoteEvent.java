package org.ijsberg.iglu.event.model;

import java.time.Instant;

public class RemoteEvent implements Event {
    private final EventType type;
    private final Instant timestampUtc;
    private final String environmentId;
    private final String assetId;
    private final String description;
    private final String message;

    public RemoteEvent(EventType type, Instant timestampUtc, String environmentId, String assetId, String description, String message) {
        this.type = type;
        this.timestampUtc = timestampUtc;
        this.environmentId = environmentId;
        this.assetId = assetId;
        this.description = description;
        this.message = message;
    }

    @Override
    public EventType getType() {
        return type;
    }

    @Override
    public Instant getTimestampUtc() {
        return timestampUtc;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    @Override
    public String getAssetId() {
        return assetId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
