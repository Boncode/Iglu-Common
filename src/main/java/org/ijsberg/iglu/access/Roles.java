package org.ijsberg.iglu.access;

import java.util.LinkedHashMap;
import java.util.Map;

public class Roles{

    public static final Long ADMINISTRATOR = 2L;

    protected static final Map<Long, BasicRole> roles = new LinkedHashMap<>();

    static {
        roles.put(ADMINISTRATOR, new BasicRole(ADMINISTRATOR, "administrator", "System role with full control (System Operations)", true)
                .with(Permissions.get(Permissions.FULL_CONTROL))
        );
    }

    public static BasicRole getRole(Long roleId) {
        return roles.get(roleId);
    }

    public static Map<Long, BasicRole> getRoles() {
        return roles;
    }
}
