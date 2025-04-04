package org.ijsberg.iglu.access;

import org.ijsberg.iglu.event.messaging.UserConsumableMessage;

import java.util.*;

import static org.ijsberg.iglu.access.Permissions.FULL_CONTROL;

/**
 * Is the transient counterpart of an account.
 * It makes the account settings available.
 * A user object can be obtained
 * by logging on to a session with proper credentials.
 * <p/>
 * When a user logs in to a realm, this object is created and stored
 * in a session.
 *
 * @see StandardSession
 */
public class BasicUser implements User {
	//a direct reference to settings in account
	private Properties settings;

	private String userId;
	private HashMap<String, Role> roles = new HashMap();
	private HashMap<String, UserGroup> groups = new HashMap<>();
	private Set<Long> groupIds = new HashSet<>();
	private final List<UserConsumableMessage> messageQueue = new ArrayList<>();


	public BasicUser(String userId, List<Role> roles, List<UserGroup> groups, Properties settings) {
		this.userId = userId;
		this.settings = settings;

		for(Role role : roles) {
			this.roles.put(role.getName().trim(), role);
		}
		for(UserGroup group : groups) {
			this.groups.put(group.getName().trim(), group);
			this.groupIds.add(group.getId());
		}
	}

	public BasicUser(String userId, Properties settings) {
		this.userId = userId;
		this.settings = settings;
	}

	public BasicUser(String userId) {
		this.userId = userId;
	}

	public final String getId() {
		return userId;
	}

	public String toString() {
		return userId;
	}

	public Properties getSettings() {
		return settings;
	}

	/**
	 * @param roleName id of the rule a user must have,
	 *               a '*' makes the method return 'true',
	 *               which can be used to check whether a user is at least authenticated
	 * @return true if a user has, or fulfills the role
	 * @see User#hasRole(String)
	 */
	public boolean hasRole(String roleName) {
		if ("*".equals(roleName)) {
			return true;
		}
		return roles.keySet().contains(roleName);
	}

	@Override
	public boolean hasOneOfRights(String ... rightsIds) {
		for(String accessRightId : rightsIds) {
			for (Role role : roles.values()) {
				if (role.hasPermission(accessRightId)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean hasAllRights(String ... rightsIds) {
		for (String accessRightId : rightsIds) {
			if (!hasOneOfRights(accessRightId)) {
				return false;
			}
		}
		return true;
	}

	public Role getRole(String roleId) {
		return roles.get(roleId);
	}

	public Collection<Role> getRoles() {
		return roles.values();
	}

    @Override
    public void addRole(Role role) {
		this.roles.put(role.getName().trim(), role);
    }

	public boolean isAccountBlocked() {
		return false;
	}

	@Override
	public UserGroup getFirstGroup() {
		if(!groups.isEmpty()) {
			return groups.values().iterator().next();
		}
		return null;
	}

	public void addGroup(UserGroup group) {
		this.groups.put(group.getName().trim(), group);
		this.groupIds.add(group.getId());
	}

	@Override
	public void dropMessage(UserConsumableMessage message) {
		synchronized (messageQueue) {
			messageQueue.add(message);
		}
	}

	@Override
	public UserConsumableMessage consumeLatestMessage() {
		synchronized (messageQueue) {
			if (!messageQueue.isEmpty()) {
				return messageQueue.remove(0);
			}
		}
		return null;
	}

	@Override
	public UserConsumableMessage getLatestMessage() {
		synchronized (messageQueue) {
			if (!messageQueue.isEmpty()) {
				return messageQueue.get(0);
			}
		}
		return null;
	}

	@Override
	public Set<String> getGroupNames() {
		return groups.keySet();
	}

	@Override
	public Set<Long> getGroupIds() {
		return groupIds;
	}

	public Set<String> getAssignedPermissionIds() {
		Set<String> retval = new HashSet<>();
		for(BasicPermission permission : getAssignedPermissions()) {
			retval.add(permission.getId());
		}
		return retval;
	}

	public Set<String> getEffectivePermissionIds() {
		Set<String> retval = new HashSet<>();
		for(BasicPermission permission : getEffectivePermissions()) {
			retval.add(permission.getId());
		}
		return retval;
	}

	public Set<BasicPermission> getEffectivePermissions() {
		Set<BasicPermission> permissions = new HashSet<>();
		LOOP:
		for (Role role : getRoles()) {
			for (String permissionId : role.listPermissionIds()) {
				if(permissionId.equals(FULL_CONTROL)) { //or x
					permissions.addAll(Permissions.all());
					break LOOP;
				}
				if(Permissions.containsId(permissionId)) {
					permissions.add(Permissions.get(permissionId));
				}
			}
		}
		return permissions;
	}

	public Set<BasicPermission> getAssignedPermissions() {
		Set<BasicPermission> permissions = new HashSet<>();
		for (Role role : getRoles()) {
			for (String permissionId : role.listPermissionIds()) {
				if(Permissions.containsId(permissionId)) {
					permissions.add(Permissions.get(permissionId));
				}
			}
		}
		return permissions;
	}

	public Set<String> getEffectivePermissionNames() {
		Set<String> retval = new HashSet<>();
		for(BasicPermission permission : getEffectivePermissions()) {
			retval.add(permission.getName());
		}
		return retval;
	}


}
