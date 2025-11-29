package org.ijsberg.iglu.event;

import org.ijsberg.iglu.event.model.EventTopic;
import org.ijsberg.iglu.event.model.EventType;

import java.util.List;
import java.util.Map;

public interface ServiceBroker {

    <T> void registerService(Class<T> interfaceType, T implementation);

    <T> void unregisterService(Class<T> interfaceType, T implementation);

    <T> List<T> getServices(Class<T> type);


    //void unsubscribe(EventListener<? extends Event> listener);
}
