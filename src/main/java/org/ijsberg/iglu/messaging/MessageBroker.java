package org.ijsberg.iglu.messaging;

import java.util.List;

public interface MessageBroker {
    <T> void registerService(Class<T> interfaceType, T implementation);

    <T> void unregisterService(Class<T> interfaceType, T implementation);

    <T> List<T> getServices(Class<T> type);
}
