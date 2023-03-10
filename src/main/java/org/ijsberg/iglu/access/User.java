/*
 * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
 *
 * This file is part of Iglu.
 *
 * Iglu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Iglu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ijsberg.iglu.access;

import org.ijsberg.iglu.messaging.UserConsumableMessage;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

/**
 * Stands for an (authenticated) application user.
 * Is the transient representation of a user account.
 */
public interface User {
	/**
	 * @return user id
	 */
	String getId();

	/**
	 * Returns properties used for personalization
	 * such as language.
	 *
	 * @return user settings or preferences
	 */
	Properties getSettings();

	/**
	 * @param roleName role ID
	 * @return true if a user has, or fulfills the role
	 */
	boolean hasRole(String roleName);

	/**
	 * @param rightsIds rights IDs
	 * @return true if a user has at least one role that possesses rights
	 */
	boolean hasOneOfRights(String ... rightsIds);

	/**
	 * @param roleId role ID
	 * @return a role by the given ID
	 */
	Role getRole(String roleId);

	/**
	 * @return all roles fulfilled by the user
	 */
	Collection<Role> getRoles();

	void addRole(Role role);

	/**
	 * @return true if the user account is marked blocked
	 */
	boolean isAccountBlocked();

	UserGroup getFirstGroup();

    void dropMessage(UserConsumableMessage message);

	UserConsumableMessage consumeLatestMessage();

	UserConsumableMessage getLatestMessage();

	Set<String> getGroupNames();

}
