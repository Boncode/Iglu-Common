package org.ijsberg.iglu.usermanagement.multitenancy.model;

import org.ijsberg.iglu.access.UserGroup;

import java.util.Set;

/**
 * Input parameters and return values of methods of modules may implement this type.
 * If the module implementation is wrapped in MultiTenantAwareComponent the methods below will be called in order to
 * ensure multitenancy safety.
 */
public interface TenantAwareInput {

    String getTenantId();

    Object filterOutOtherTenants(Set<String> userGroupNames);
}
