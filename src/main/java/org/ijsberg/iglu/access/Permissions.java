package org.ijsberg.iglu.access;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Permissions {

    public static final String FULL_CONTROL = "FULL_CONTROL";

    public static final String R = "r";
    public static final String W = "w";
    public static final String X = "x";

    protected static final Map<String, BasicPermission> permissionMap = new LinkedHashMap<>();

    protected static String register(String id, String name, String description) {
        BasicPermission permission = new BasicPermission(id, name, description);
        permissionMap.put(id, permission);
        return id;
    }

    static {
        register(FULL_CONTROL, "full control", "Administrator rights");
        register(R, "read", "Unix-style read access, typically for viewing content only");
        register(W, "write", "Unix-style write access, typically for creating and modifying content");
        register(X, "execute", "Unix-style execute access, typically for managing configurations");
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
