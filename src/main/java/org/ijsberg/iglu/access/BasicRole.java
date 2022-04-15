package org.ijsberg.iglu.access;

import org.ijsberg.iglu.util.collection.CollectionSupport;
import org.ijsberg.iglu.util.misc.StringSupport;

import java.util.List;

public class BasicRole implements Role {

    private long id;
    private String name;
    private String description;
    private String accessRightIds = "";

    public BasicRole(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public BasicRole(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public BasicRole with(AccessRight right) {
        List<String> accessRightIdList = StringSupport.split(accessRightIds, ",");
        if(!accessRightIdList.contains(right.getId())) {
            accessRightIdList.add(right.getId());
            accessRightIds = CollectionSupport.format(accessRightIdList, ",");
        }
        return this;
    }

    public long getId() {
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

    public boolean hasRight(String accessRightId) {
        List<String> accessRightIdList = StringSupport.split(accessRightIds, ",");
        return accessRightIdList.contains(accessRightId);
    }
}
