package org.ijsberg.iglu.event.model;

import java.time.Instant;

public interface Event {

    EventType getType();

    Instant getTimestampUtc();

    String getAssetId();

    String getDescription();

    String getMessage();
}
