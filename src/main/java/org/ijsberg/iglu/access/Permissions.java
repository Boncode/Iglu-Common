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
        register(FULL_CONTROL, "full control", "administrator rights");
        register(R, "read", "read");
        register(W, "write", "write");
        register(X, "execute", "execute");
    }

    public static BasicPermission get(String id) {
        return permissionMap.get(id);
    }

    public static Collection<BasicPermission> all() {
        return permissionMap.values();
    }
}
