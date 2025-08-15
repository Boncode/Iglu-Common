package org.ijsberg.iglu.event;

import org.ijsberg.iglu.event.model.Event;

public interface EventListener<T extends Event> {

    void onEvent(T event);
}
