package org.ijsberg.iglu.access;

public class BasicAccessRight implements AccessRight {
    private String id;
    private String name;
    private String description;

    public BasicAccessRight(String id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
