package org.ijsberg.iglu.event;

public class FindingForEventTrigger {

    private float value;
    private float comparingValue;
    BasicEventTrigger trigger;

    public FindingForEventTrigger(float value, float comparingValue, BasicEventTrigger trigger) {
        this.value = value;
        this.comparingValue = comparingValue;
        this.trigger = trigger;
    }

    public float getValue() {
        return value;
    }

    public float getComparingValue() {
        return comparingValue;
    }

    public BasicEventTrigger getTrigger() {
        return trigger;
    }

}
