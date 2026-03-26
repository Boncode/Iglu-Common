package org.ijsberg.iglu.event.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

public class BasicEventDto {

    private final String eventTypeId;
    private final Instant timestampUtc;
    private final String assetId;

    @JsonCreator
    public BasicEventDto(
        @JsonProperty(value = "eventTypeId") String eventTypeId,
        @JsonProperty(value = "timestampUtc") Instant timestampUtc,
        @JsonProperty(value = "assetId") String assetId) {
        this.eventTypeId = eventTypeId;
        this.timestampUtc = timestampUtc;
        this.assetId = assetId;
    }

    public String getEventTypeId() {
        return eventTypeId;
    }

    public Instant getTimestampUtc() {
        return timestampUtc;
    }

    public String getAssetId() {
        return assetId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BasicEventDto eventDto = (BasicEventDto) o;
        return Objects.equals(getEventTypeId(), eventDto.getEventTypeId()) && Objects.equals(getTimestampUtc(), eventDto.getTimestampUtc());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEventTypeId(), getTimestampUtc());
    }
}
