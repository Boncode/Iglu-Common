package org.ijsberg.iglu.event.module;

import org.ijsberg.iglu.event.EventBus;
import org.ijsberg.iglu.event.EventListener;
import org.ijsberg.iglu.event.ServiceBroker;
import org.ijsberg.iglu.event.model.Event;
import org.ijsberg.iglu.event.model.EventTopic;
import org.ijsberg.iglu.event.model.EventType;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.scheduling.Pageable;
import org.ijsberg.iglu.util.collection.ListHashMap;
import org.ijsberg.iglu.util.collection.ListMap;
import org.ijsberg.iglu.util.collection.ListTreeMap;
import org.ijsberg.iglu.util.time.TimePeriod;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.*;

import static org.ijsberg.iglu.util.time.TimeUnit.DAY;

public class BasicServiceBroker implements ServiceBroker, EventBus, Pageable {

    private final ListMap<Class<?>, Object> serviceMap = new ListHashMap<>();

    private static final TimePeriod EVENT_MESSAGE_TIME_PERIOD_TO_STORE = new TimePeriod(1, DAY);
    private final ListTreeMap<Instant, Event> latestEvents = new ListTreeMap<>();

    private final ListMap<EventTopic<? extends Event>, EventListener<? extends Event>> eventListenersByTopic = new ListHashMap<>();
    private final ListMap<EventType, EventTopic<? extends Event>> topicsByEventType = new ListHashMap<>();
    private final Set<EventTopic<? extends Event>> allTopics = new HashSet<>();

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

    @Override
    public void registerEventTopic(EventTopic<? extends Event> eventTopic, EventType... eventTypes) {
        allTopics.add(eventTopic);
        for(EventType eventType : eventTypes) {
            topicsByEventType.putDistinct(eventType, eventTopic);
        }
    }

    @Override
    public void subscribe(EventTopic<? extends Event> topic, EventListener<? extends Event> listener) {
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
        synchronized(latestEvents) {
            latestEvents.putDistinct(event.getTimestampUtc(), event);
        }

        List<EventTopic<? extends Event>> topicsForType = topicsByEventType.get(event.getType());
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

    @Override
    public Map<EventTopic<? extends Event>, List<Event>> getLatestEvents() {
        Map<EventTopic<? extends Event>, List<Event>> latestEventByTopic = new HashMap<>();
        synchronized(latestEvents) {
            for(Event event : latestEvents.valuesDescending()) {
                List<EventTopic<? extends Event>> topicsForEvent = topicsByEventType.get(event.getType());
                for(EventTopic<? extends Event> topic : topicsForEvent) {
                    if(latestEventByTopic.containsKey(topic)) {
                        latestEventByTopic.get(topic).add(event);
                    } else {
                        List<Event> list = new ArrayList<>();
                        list.add(event);
                        latestEventByTopic.put(topic, list);
                    }
                }
            }
            return latestEventByTopic;
        }
    }

    private <T extends Event> boolean checkEventTypeValidityForTopic(EventTopic<? extends Event> topic, T event) {
        if(!topic.eventClass().isAssignableFrom(event.getClass())) {
            System.out.println(new LogEntry(Level.DEBUG, "event of type " + event.getClass().getSimpleName() +  " not a (sub)type of " + topic.eventClass().getSimpleName()));
            return false;
        }
        return true;
    }

    @Override
    public int getPageIntervalInMinutes() {
        return 60;
    }

    @Override
    public int getPageOffsetInMinutes() {
        return 0;
    }

    @Override
    public void onPageEvent(long officialTime) {
        Instant timeUtcToKeepEntries = Instant.now().minus(EVENT_MESSAGE_TIME_PERIOD_TO_STORE.getLength(), EVENT_MESSAGE_TIME_PERIOD_TO_STORE.getTimeUnit().getTemporalUnit());
        synchronized(latestEvents) {
            Set<Instant> keySet = new LinkedHashSet<>(latestEvents.keySet());
            for (Instant key : keySet) {
                if (key.isBefore(timeUtcToKeepEntries)) {
                    latestEvents.removeAll(key);
                }
            }
        }
    }

    @Override
    public boolean isStarted() {
        return true;
    }
}
