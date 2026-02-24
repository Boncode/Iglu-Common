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

/**
 * Marks the location where a client request enters the
 * application. Implementations are typically http-servlets
 * or -filters.
 * <p/>
 * Implementations are responsible of binding and releasing the
 * user request to their threads of execution.
 */
public interface EntryPoint {
	/**
	 * Is invoked when a session is created somewhere inside the application.
	 *
	 * @param currentRequest request that must be bound to current thread
	 * @param session        the newly created session
	 */
	void onSessionUpdate(Request currentRequest, Session session);

	/**
	 * Is invoked when a session is destroyed somewhere inside the application.
	 *
	 * @param currentRequest request that must be bound to current thread
	 * @param session        session that gets destroyed
	 */
	void onSessionDestruction(Request currentRequest, Session session);

}
