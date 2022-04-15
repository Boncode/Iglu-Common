package org.ijsberg.iglu.access;

import java.util.LinkedHashMap;
import java.util.Map;

public class AccessRights {

    public static final String FULL_CONTROL = "FULL_CONTROL";

    public static final String R = "r";
    public static final String W = "w";
    public static final String X = "x";

    protected static final Map<String, AccessRight> accessRightMap = new LinkedHashMap<>();

    static {
        accessRightMap.put(FULL_CONTROL, new BasicAccessRight(FULL_CONTROL, "full control", "administrator rights"));
        accessRightMap.put(R, new BasicAccessRight(R, "read", "read"));
        accessRightMap.put(W, new BasicAccessRight(W, "write", "write"));
        accessRightMap.put(X, new BasicAccessRight(X, "execute", "execute"));
    }

    public static AccessRight get(String id) {
        return accessRightMap.get(id);
    }
}
