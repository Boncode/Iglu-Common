package org.ijsberg.iglu.messaging.module;

import org.ijsberg.iglu.messaging.MessageBroker;
import org.ijsberg.iglu.messaging.ServiceProxy;
import org.ijsberg.iglu.util.collection.ListHashMap;
import org.ijsberg.iglu.util.collection.ListMap;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class BasicMessageBroker implements MessageBroker {

    private final ListMap<Class<?>, Object> serviceMap = new ListHashMap<>();

    @Override
    public <T> void registerService(Class<T> interfaceType, T implementation) {
        serviceMap.put(interfaceType, implementation);
    }

    @Override
    public <T> void unregisterService(Class<T> interfaceType, T implementation) {
        serviceMap.remove(interfaceType, implementation);
    }

    @Override
    public <T> List<T> getServices(Class<T> type) {
        List<T> services = new ArrayList<>();
        List<Object> implementations = serviceMap.get(type);
        if(implementations != null) {
            for (Object o : implementations) {
                services.add((T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new ServiceProxy(o)));
            }
        }
        return services;
    }
}
