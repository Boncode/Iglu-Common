package org.ijsberg.iglu.event;

public class BasicEventTrigger {

    public enum ConditionType {
        ABOVE_THRESHOLD,
        BELOW_THRESHOLD,
        ABOVE_PREVIOUS,
        BELOW_PREVIOUS,
        PERCENTAGE_GREATER_THAN,
        PERCENTAGE_SMALLER_THAN,
        HAS_CHANGED
    }

    private long id;
    private String name;
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


    public BasicEventTrigger(String metricId, String name, Float threshold, ConditionType conditionType) {
        id = System.currentTimeMillis();
        this.name = name;
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

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
}
