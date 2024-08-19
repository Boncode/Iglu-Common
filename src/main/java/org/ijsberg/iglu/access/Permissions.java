package org.ijsberg.iglu.access;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Permissions {

    public static final String FULL_CONTROL = "FULL_CONTROL";
    public static final String UPLOAD = "UPLOAD";
    public static final String TENANT_SURPASSING = "TENANT_SURPASSING";

    protected static final Map<String, BasicPermission> permissionMap = new LinkedHashMap<>();

    protected static String register(String id, String name, String description) {
        BasicPermission permission = new BasicPermission(id, name, description);
        permissionMap.put(id, permission);
        return id;
    }

    static {
        register(FULL_CONTROL, "full control", "Administrator rights");
        register(TENANT_SURPASSING, "tenant and user group surpassing", "No restrictions regarding multitenancy, access to resources and functions of all user groups");
        register(UPLOAD, "generic file uploads", "Generic file uploads");
    }

    public static BasicPermission get(String id) {
        return permissionMap.get(id);
    }

    public static Collection<BasicPermission> all() {
        return permissionMap.values();
    }

    public static boolean containsId(String id) {
        return permissionMap.containsKey(id);
    }
}
