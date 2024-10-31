package org.ijsberg.iglu.event.messaging.message;

import org.ijsberg.iglu.event.EventTopic;
import org.ijsberg.iglu.event.EventType;
import org.ijsberg.iglu.event.messaging.EventMessage;

import java.time.Instant;
import java.util.Objects;

public class BasicEventMessage implements EventMessage {

    private EventTopic eventTopic;

    private EventType eventType;

    //now.toString(): 2014-01-21T23:42:03.522Z
    private Instant timestampUtc;

    private String location = "localhost";
    private String message;


    public BasicEventMessage(EventTopic eventTopic, EventType eventType, String message) {
        this.eventTopic = eventTopic;
        this.eventType = eventType;
        this.message = message;
        this.timestampUtc = Instant.now();
    }

    public BasicEventMessage(EventTopic eventTopic, EventType eventType, String message, String location) {
        this.eventTopic = eventTopic;
        this.eventType = eventType;
        this.message = message;
        this.location = location;
        this.timestampUtc = Instant.now();
    }

    public BasicEventMessage(EventTopic eventTopic, EventType eventType, String message, Instant timestampUtc) {
        this.eventTopic = eventTopic;
        this.eventType = eventType;
        this.message = message;
        this.timestampUtc = timestampUtc;
    }

    public EventTopic getTopic() {
        return eventTopic;
    }

    public EventType getType() {
        return eventType;
    }

    public Instant getTimestampUtc() {
        return timestampUtc;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicEventMessage that = (BasicEventMessage) o;
        return eventType.equals(that.eventType) && timestampUtc.equals(that.timestampUtc) && location.equals(that.location) && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, timestampUtc, location, message);
    }

    @Override
    public String toString() {
        return "BasicEventMessage{" +
                "eventTopic=" + eventTopic +
                ", eventType=" + eventType +
                ", timestampUtc=" + timestampUtc +
                ", location='" + location + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
