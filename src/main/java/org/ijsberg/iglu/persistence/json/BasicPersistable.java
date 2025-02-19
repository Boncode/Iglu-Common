package org.ijsberg.iglu.persistence.json;

public abstract class BasicPersistable {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
