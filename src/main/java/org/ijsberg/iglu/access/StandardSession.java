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

import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.util.io.ReceiverQueue;
import org.ijsberg.iglu.util.misc.KeyGenerator;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * A sessiom contains stateful data and access in the form of agents.
 * <p/>
 * A session may contain receivers and transceivers which can be used to
 * communicate messages (asynchronously), such as dedicated log messages.
 *
 * @see AccessManager
 */
public final class StandardSession implements Serializable, Session//, PropertyListener
{
	private String token;
	private Date creationTime = new Date();
	//http-session style attributes
	private final TreeMap attributes = new TreeMap();
	//default settings, possibly merged with settings
	//whenever a session is created, the StandardRequestManager adds its default settings to the created session
	private Properties userSettings;
	//collection of session objects for each realm
	private HashMap<String, Component> agentComponents = new HashMap();
	//tme last accessed, needed for timeout determination
	private long lastAccessedTime = System.currentTimeMillis();
	//expiration timeout in milliseconds
	private long expirationTimeout;//expires straight away by default
	private long expirationTimeoutLoggedIn;//expires straight away by default
	private User user;

//	private HashMap forms;

	private AccessManager accessManager;

	/**
	 * Constructs a normal user session.
	 *
	 * @param expirationTimeout expiration timeout in seconds, 0 or less means that session will not expire
	 */
	public StandardSession(AccessManager accessManager, long expirationTimeout, long expirationTimeoutLoggedIn, Properties defaultUserSettings) {
		//create an id that's unique and difficult to guess
		token = KeyGenerator.generateKey();
//		this.application = application;
		//store as millis
		this.expirationTimeout = expirationTimeout * 1000;
		this.expirationTimeoutLoggedIn = expirationTimeoutLoggedIn * 1000;
		this.accessManager = accessManager;
		//copy settings
//		userSettings = new GenericPropertyBundle("user settings");
//		userSettings.merge(defaultUserSettings);
//		userSettings.setListener(this);
		userSettings = defaultUserSettings;
	}

	public StandardSession(String token, AccessManager accessManager, long expirationTimeout, long expirationTimeoutLoggedIn, Properties defaultUserSettings) {
		//create an id that's unique and difficult to guess
		this.token = token;
//		this.application = application;
		//store as millis
		this.expirationTimeout = expirationTimeout * 1000;
		this.expirationTimeoutLoggedIn = expirationTimeoutLoggedIn * 1000;
		this.accessManager = accessManager;
		//copy settings
//		userSettings = new GenericPropertyBundle("user settings");
//		userSettings.merge(defaultUserSettings);
//		userSettings.setListener(this);
		userSettings = defaultUserSettings;
	}

	/**
	 * @return session token used to identify a session over a number of requests
	 */
	public String getToken() {
		return token;
	}


	/**
	 * @return
	 */
	public Date getCreationTime() {
		return creationTime;
	}


	/**
	 * Receivers are used to receive (a stream of) messages.
	 * Receivers are stored as regular attributes on the session.
	 *
	 * @return a list of receivers by filtering this session's attributes
	 * @see ReceiverQueue
	 */
	public List getReceivers() {
		ArrayList result = new ArrayList();
		synchronized (attributes) {
			Iterator i = attributes.values().iterator();
			while (i.hasNext()) {
				Object o = i.next();
				if (o instanceof ReceiverQueue) {
					result.add(o);
				}
			}
		}
		return result;
	}


	/**
	 * @return
	 */
	public Properties getUserSettings() {
		if (userSettings == null) {
			userSettings = new Properties();
		}
		return userSettings;
	}

	/**
	 * Merges settings with the existing settings.
	 */
/*	public void addSettings(GenericPropertyBundle settings)
	{
		this.userSettings.merge(settings);
	}*/
	@Override
	public <T> Component getAgent(String id) {

		if (agentComponents.containsKey(id)) {
			return agentComponents.get(id);
		}
		return createAgent(id);
	}

	private <T> Component createAgent(String id) {
		Component agent = accessManager.createAgent(id);
		agentComponents.put(id, agent);
		return agent;
	}


	/**
	 * @return a user logged in to the current realm
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Authenticates a user for a certain realm based on credentials.
	 *
	 * @param credentials
	 * @return transient user if authentication succeeded or null if it didn't
	 * @throws SecurityException       if user already logged in
	 * @throws AuthenticationException if some user action is required
	 */
	public User login(Credentials credentials) throws SecurityException {
		//check if user is logged in already
		User loggedInUser = getUser();
		if (loggedInUser != null) {
			logout();
			//throw new SecurityException("user already logged in as '" + loggedInUser.getId() + "'");
		}

		User user = accessManager.authenticate(credentials);
		if (user != null) {
			this.user = user;
			userSettings = user.getSettings();
		}

		return user;
	}

	@Override
	public User loginAsSystem(String userId) {
		user = accessManager.getSystemUser(userId);
		if(user != null) {
			userSettings = user.getSettings();
		}
		return user;
	}

	@Override
	public User loginAsSystem(String userId, long expirationTimeoutLoggedIn) {
		this.expirationTimeoutLoggedIn = expirationTimeoutLoggedIn * 1000;
		return loginAsSystem(userId);
	}


	/**
	 * Performs destruction of agents and closes message receivers.
	 */
	public void onDestruction() {
		for (String id : agentComponents.keySet()) {
			Component component = agentComponents.get(id);
			System.out.println(component + " " + Arrays.asList(component.getInterfaces()).contains(SessionDestructionListener.class));
			if(Arrays.asList(component.getInterfaces()).contains(SessionDestructionListener.class)) {
				try {
					component.invoke("onSessionDestruction");
				} catch (InvocationTargetException e) {
					throw new ConfigurationException(e);
				} catch (NoSuchMethodException e) {
					throw new ConfigurationException(e);
				}
			}
			accessManager.removeAgent(id, component);
		}
		attributes.clear();
	}

	/**
	 * Performs logout of user in the current realm.
	 */
	public void logout() {
		user = null;
	}

	/**
	 * Renews the last-accessed-time and prolongs the life of this session.
	 */
	public void updateLastAccessedTime() {
		lastAccessedTime = System.currentTimeMillis();
	}

	/**
	 * @return true if this session should be considered expired based on the last-accessed-time and timeout value
	 */
	public boolean isExpired() {

		long usedTimeOut = expirationTimeout;
		if(this.getUser() != null) {
			usedTimeOut = expirationTimeoutLoggedIn;
		}

		boolean isExpired = lastAccessedTime > 0 && lastAccessedTime + usedTimeOut < System.currentTimeMillis();
//		System.out.println(new LogEntry("last Accessed:  " + lastAccessedTime + ":" + new Date(lastAccessedTime) + " timeout: " + usedTimeOut));
//		System.out.println(new LogEntry("session " + getToken() + " will expire in " + ((lastAccessedTime + usedTimeOut) - System.currentTimeMillis())));
//		System.out.println(new LogEntry("session " + getToken() + " will expire on " + new Date(lastAccessedTime + usedTimeOut)));
		return isExpired;
	}


	/**
	 * @return session description
	 */
	public String toString() {
		StringBuffer result = new StringBuffer(creationTime.toString());
		result.append(' ' + token);
		for(Component agent : agentComponents.values()) {
			result.append(' ' + agent.toString());
		}
		return result.toString();
	}

	/**
	 * Stores object during the lifespan of the request.
	 * Use with care.
	 *
	 * @param key
	 * @param value
	 */
	public void setAttribute(Object key, Object value) {
		attributes.put(key, value);
	}

	/**
	 * Retrieves stored object.
	 *
	 * @param key
	 * @return attribute stored under the key or null
	 */
	public Object getAttribute(Object key) {
		if (attributes == null) {
			return null;
		}
		return attributes.get(key);
	}


}
