package org.ijsberg.iglu.event;

public class BasicEventTrigger {

    public enum ConditionType {
        LARGER_THAN,
        SMALLER_THAN,
        EQUALS;
    }

    private long id;
    private String metricId;
    private Float threshold;
    private ConditionType conditionType;

    public BasicEventTrigger() {}

    /*public BasicEventTrigger(long id, String metricId, Float threshold, BasicEventTrigger.conditionType conditionType) {
        this.id = id;
        this.metricId = metricId;
        this.threshold = threshold;
        this.conditionType = conditionType;
    }*/


    public BasicEventTrigger(String metricId, Float threshold, ConditionType conditionType) {
        id = System.currentTimeMillis();
        this.metricId = metricId;
        this.threshold = threshold;
        this.conditionType = conditionType;
    }

    public long getId() {
        return id;
    }

    public String getMetricId() {
        return metricId;
    }

    public void setMetricId(String metricId) {
        this.metricId = metricId;
    }

    public Float getThreshold() {
        return threshold;
    }

    public void setThreshold(Float threshold) {
        this.threshold = threshold;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }
}
