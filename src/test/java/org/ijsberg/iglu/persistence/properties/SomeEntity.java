package org.ijsberg.iglu.persistence.properties;

import java.util.Map;

public class SomeEntity {
    private long id;
    private String name = "bogus";
    private int value;
    private boolean bool = true;
    private Map<String, Map<String, String>> map;

    public SomeEntity() {
    }

    public SomeEntity(String name, int value, boolean bool) {
        this.name = name;
        this.value = value;
        this.bool = bool;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public boolean isBool() {
        return bool;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

    @Override
    public String toString() {
        return "SomeEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", value=" + value +
                ", map=" + map +
                '}';
    }

    public void setMap(Map<String, Map<String, String>> map) {
        this.map = map;
    }

    public Map<String, Map<String, String>> getMap() {
        return map;
    }
}
