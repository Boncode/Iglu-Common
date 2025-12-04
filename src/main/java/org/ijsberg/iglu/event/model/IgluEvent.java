package org.ijsberg.iglu.event.model;

import java.time.Instant;

public class IgluEvent extends BasicEvent {

    public enum IgluEventType implements EventType {

        LOGGING_STARTED,
        LOGFILE_ROTATED;

        @Override
        public String getId() {
            return name();
        }

        @Override
        public String getLabel() {
            return getEventTypeLabel(this);

        }
    }

    public IgluEvent(IgluEventType type, Instant timestampUtc, String assetId) {
        super(type, timestampUtc, assetId);
    }
}
