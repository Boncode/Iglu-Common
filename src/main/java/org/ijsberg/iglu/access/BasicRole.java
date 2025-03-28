package org.ijsberg.iglu.access;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ijsberg.iglu.util.collection.CollectionSupport;
import org.ijsberg.iglu.util.misc.StringSupport;

import java.util.List;
import java.util.Objects;

public class BasicRole implements Role {

    private long id;
    private String name;
    private String description;
    private String permissionIds = "";

    @JsonProperty("systemRole")
    private boolean isSystemRole = false;


    public BasicRole() {}

    public BasicRole(String name, String description) {
        this.name = name;
        this.description = description;
    }


    public BasicRole(long id, String name, String description, boolean isSystemRole) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isSystemRole = isSystemRole;
    }

    public BasicRole with(Permission permission) {
        List<String> permissionIdList = StringSupport.split(permissionIds, ",");
        if(!permissionIdList.contains(permission.getId())) {
            permissionIdList.add(permission.getId());
            permissionIds = CollectionSupport.format(permissionIdList, ",");
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

    @Override
    public boolean hasPermission(String permissionId) {
        List<String> permissionIdList = StringSupport.split(permissionIds, ",");
        return permissionIdList.contains(permissionId);
    }

    @Override
    public List<String> listPermissionIds() {
        return StringSupport.split(permissionIds, ",");
    }

    public String getPermissionIds() {
        return permissionIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicRole basicRole = (BasicRole) o;
        return id == basicRole.id && Objects.equals(name, basicRole.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

//    public void addPermission(String permissionId) {
//        List<String> permissionIdList = listPermissionIds();
//        if(!permissionIdList.contains(permissionId)) {
//            permissionIdList.add(permissionId);
//            permissionIds = CollectionSupport.format(permissionIdList, ",");
//        }
//    }

    public boolean isSystemRole() {
        return isSystemRole;
    }

    @Override
    public String toString(){
        return "BasicRole{id=" + id +
                ", name=" + name +
                ", description=" + description +
                ", permissionIds=" + permissionIds +
                ", isSystemRole=" + isSystemRole +
                "}";
    }
}
