package org.ijsberg.iglu.access;

public class BasicRole implements Role {
    private String id;
    private String description;

    public BasicRole(String id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
