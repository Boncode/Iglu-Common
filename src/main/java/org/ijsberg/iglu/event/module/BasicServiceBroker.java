package org.ijsberg.iglu.event.module;

import org.ijsberg.iglu.event.EventBus;
import org.ijsberg.iglu.event.EventListener;
import org.ijsberg.iglu.event.ServiceBroker;
import org.ijsberg.iglu.event.model.Event;
import org.ijsberg.iglu.event.model.EventTopic;
import org.ijsberg.iglu.event.model.EventType;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.util.collection.ListHashMap;
import org.ijsberg.iglu.util.collection.ListMap;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;

import java.lang.reflect.Proxy;
import java.util.*;

public class BasicServiceBroker implements ServiceBroker, EventBus {

    private final ListMap<Class<?>, Object> serviceMap = new ListHashMap<>();

    private final ListMap<EventTopic<? extends Event>, EventListener<? extends Event>> eventListenersByTopic = new ListHashMap<>();
    //private final ListMap<EventType, EventTopic<? extends Event>> topicsByEventType = new ListHashMap<>();
    private final Set<EventTopic<? extends Event>> allTopics = new HashSet<>();

    private final ListMap<Class<? extends Event>, EventTopic> topicsByEventClass = new ListHashMap<>();


    public BasicServiceBroker() {
    }

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

    private Map<EventTopic, List<EventType>> eventTypesByTopic = new HashMap<>();

    @Override
    public void registerEventTopic(EventTopic<? extends Event> eventTopic) {
        allTopics.add(eventTopic);
        /*for(EventType eventType : eventTypes) {
            topicsByEventType.putDistinct(eventType, eventTopic);
        }*/
        List<EventType> eventTypes = ReflectionSupport.getInnerEnumTypes(eventTopic.eventClass(), EventType.class);
        eventTypesByTopic.put(eventTopic, eventTypes);
        topicsByEventClass.put(eventTopic.eventClass(), eventTopic);
    }

    @Override
    public Map<EventTopic, List<EventType>> getEventTypesByTopic() {
        return eventTypesByTopic;
    }

    @Override
    public List<EventTopic> getTopicsForEventType(Class<? extends Event> eventType) {
        return topicsByEventClass.get(eventType);
    }


    @Override
    public <T extends Event> void subscribe(EventTopic<T> topic, EventListener<T> listener) {
        synchronized(eventListenersByTopic) {
            eventListenersByTopic.putDistinct(topic, listener);
        }
    }

    @Override
    public void subscribeToAll(EventListener<? extends Event> listener) {
        synchronized(allTopics) {
            for(EventTopic topic : allTopics) {
                eventListenersByTopic.putDistinct(topic, listener);
            }
        }
    }

    @Override
    public void unsubscribe(EventListener<? extends Event> listener) {
        synchronized(allTopics) {
            for(EventTopic topic : allTopics) {
                unsubscribe(topic, listener);
            }
        }
    }

    @Override
    public void unsubscribe(EventTopic<? extends Event> topic, EventListener<? extends Event> listener) {
        synchronized(eventListenersByTopic) {
            eventListenersByTopic.remove(topic, listener);
        }
    }

    @Override
    public <T extends Event> void publish(T event) {

//        List<EventTopic<? extends Event>> topicsForType = topicsByEventType.get(event.getType());
        List<EventTopic> topicsForType = topicsByEventClass.get(event.getClass());
        if(topicsForType != null) {
            for (EventTopic<? extends Event> topic : topicsForType) {
                if(checkEventTypeValidityForTopic(topic, event)) {
                    System.out.println(new LogEntry(Level.DEBUG, "Publishing event in topic [" + topic.id() + "] " + event.getType().getId()));
                    synchronized (eventListenersByTopic) {
                        List<EventListener<T>> listeners = getEventListenersForTopic(topic);
                        for (EventListener<T> eventListener : new ArrayList<>(listeners)) {
                            forwardEvent(event, eventListener);
                        }
                    }
                } else {
                    System.out.println(new LogEntry(Level.CRITICAL, "NOT publishing event in topic [" + topic.id() + "] " + event.getType().getId()
                            + " as it is not compatible with expected class type: " + topic.eventClass().getSimpleName()));
                }
            }
        }
    }

    private static <T extends Event> void forwardEvent(T event, EventListener<T> eventListener) {
        try {
            long start = System.currentTimeMillis();
            eventListener.onEvent(event);
            long end = System.currentTimeMillis();
            if(end - start > 100) {
                System.out.println(new LogEntry(Level.CRITICAL, "handling forwarded event of type " + event.getType() + " by " + eventListener.getClass().getSimpleName() + " took " + (end - start) + " ms (> 100)"));
            }
        } catch (Throwable t) {
            System.out.println(new LogEntry(Level.CRITICAL, "handling forwarded event of type " + event.getType() + " by " + eventListener.getClass().getSimpleName() + " failed", t));
        }
    }

    private <T extends Event> List<EventListener<T>> getEventListenersForTopic(EventTopic<? extends Event> topic) {
        List<EventListener<T>> eventListeners = new ArrayList<>();
        List<EventListener<? extends Event>> eventListenersForTopic = eventListenersByTopic.get(topic);
        if(eventListenersForTopic != null) {
            for(EventListener<? extends Event> listener : eventListenersForTopic) {
                eventListeners.add(((EventListener<T>)listener));
            }
        }
        return eventListeners;
    }

    private <T extends Event> boolean checkEventTypeValidityForTopic(EventTopic<? extends Event> topic, T event) {
        if(!topic.eventClass().isAssignableFrom(event.getClass())) {
            System.out.println(new LogEntry(Level.DEBUG, "event of type " + event.getClass().getSimpleName() +  " not a (sub)type of " + topic.eventClass().getSimpleName()));
            return false;
        }
        return true;
    }


}
