package org.ijsberg.iglu.event.module;

import org.ijsberg.iglu.event.EventListener;
import org.ijsberg.iglu.event.EventTopic;
import org.ijsberg.iglu.event.ServiceBroker;
import org.ijsberg.iglu.event.messaging.EventMessage;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.util.collection.ListHashMap;
import org.ijsberg.iglu.util.collection.ListMap;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class BasicServiceBroker implements ServiceBroker {

    private final ListMap<Class<?>, Object> serviceMap = new ListHashMap<>();

    private final ListMap<EventTopic, EventListener> eventListenerMap = new ListHashMap<>();

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

    @Override
    public void subscribe(EventTopic topic, EventListener listener) {
        eventListenerMap.put(topic, listener);
    }

    @Override
    public void unsubscribe(EventTopic topic, EventListener listener) {
        eventListenerMap.remove(topic, listener);
    }

    @Override
    public void publish(EventMessage message) {
        for(EventListener eventListener : new ArrayList<>(eventListenerMap.get(message.getTopic()))) {
            try {
                eventListener.onEvent(message);
            } catch (Exception e) {
                System.out.println(new LogEntry(Level.CRITICAL, "failed to forward event of type " + message.getType(), e));
            }
        }
    }

}
