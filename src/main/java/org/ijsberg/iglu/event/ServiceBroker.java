package org.ijsberg.iglu.event;

import org.ijsberg.iglu.event.messaging.EventMessage;

import java.util.List;

public interface ServiceBroker {

    <T> void registerService(Class<T> interfaceType, T implementation);

    <T> void unregisterService(Class<T> interfaceType, T implementation);

    <T> List<T> getServices(Class<T> type);

    void subscribe(EventTopic eventTypesToReceive, EventListener listener);

    void unsubscribe(EventTopic eventTypesToReceive, EventListener listener);

    void publish(EventMessage message);
}
