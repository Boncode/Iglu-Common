package org.ijsberg.iglu.event;

import org.ijsberg.iglu.event.messaging.EventMessage;

public interface EventListener {
    void onEvent(EventMessage eventMessage);
}
