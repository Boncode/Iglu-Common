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

package org.ijsberg.iglu.usermanagement;

import org.ijsberg.iglu.access.AuthenticationException;
import org.ijsberg.iglu.access.Credentials;
import org.ijsberg.iglu.access.User;
import org.ijsberg.iglu.usermanagement.domain.JsonSimpleAccount;

import java.util.List;

/**
 */
public interface UserManager {


	void addAccount(String userId, String password);

	List<JsonSimpleAccount> getAccounts();

	void addAccount(User user, String password);

	void updateAccount(User user);

//	void updateAccounts(List<User> users);

	void resetPassword(String userId, String newPassword);

	void resetPassword(String userId, String oldPassword, String newPassword);

	void removeAccount(String userId);

	boolean setGroup(String userId, String groupId);

	List<String> listAccounts();

	User authenticate(Credentials credentials) throws AuthenticationException;
}
