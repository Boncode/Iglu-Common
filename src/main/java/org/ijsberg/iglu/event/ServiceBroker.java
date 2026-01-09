package org.ijsberg.iglu.event;

import java.util.List;

public interface ServiceBroker {

    <T> void registerService(Class<T> interfaceType, T implementation);

    <T> void unregisterService(Class<T> interfaceType, T implementation);

    <T> List<T> getServices(Class<T> type);
}
