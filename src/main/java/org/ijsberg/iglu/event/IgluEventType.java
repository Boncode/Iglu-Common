package org.ijsberg.iglu.event;

import org.ijsberg.iglu.event.model.EventType;

public enum IgluEventType implements EventType {

    LOGGING_STARTED,
    LOGFILE_ROTATED;

    @Override
    public String getId() {
        return name();
    }

    @Override
    public String getLabel() {
        return name().replaceAll("_", " ").toLowerCase();
    }
}
