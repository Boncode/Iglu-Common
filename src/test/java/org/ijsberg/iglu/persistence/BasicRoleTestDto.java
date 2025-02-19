package org.ijsberg.iglu.persistence;

import org.ijsberg.iglu.access.Permission;
import org.ijsberg.iglu.access.Role;
import org.ijsberg.iglu.util.collection.CollectionSupport;
import org.ijsberg.iglu.util.misc.StringSupport;

import java.util.List;
import java.util.Objects;

public class BasicRoleTestDto implements Role {

    private long id;
    private String name;
    private String description;
    private String permissionIds = "";

    private boolean systemRole = false;


    public BasicRoleTestDto() {}

    public BasicRoleTestDto(String name, String description) {
        this.name = name;
        this.description = description;
    }


    public BasicRoleTestDto(long id, String name, String description, boolean systemRole) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.systemRole = systemRole;
    }

    public BasicRoleTestDto with(Permission permission) {
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
        BasicRoleTestDto basicRole = (BasicRoleTestDto) o;
        return getId() == basicRole.getId() && Objects.equals(name, basicRole.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), name);
    }

    public void addPermission(String permissionId) {
        List<String> permissionIdList = listPermissionIds();
        if(!permissionIdList.contains(permissionId)) {
            permissionIdList.add(permissionId);
            permissionIds = CollectionSupport.format(permissionIdList, ",");
        }
    }

    public boolean isSystemRole() {
        return systemRole;
    }

    @Override
    public String toString(){
        return "BasicRoleTestDto{id=" + getId() +
                ", name=" + name +
                ", description=" + description +
                ", permissionIds=" + permissionIds +
                ", systemRole=" + systemRole +
                "}";
    }
}
