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

package org.ijsberg.iglu.usermanagement.module;

import org.ijsberg.iglu.access.*;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.usermanagement.Account;
import org.ijsberg.iglu.usermanagement.UserManager;
import org.ijsberg.iglu.usermanagement.domain.SimpleAccount;
import org.ijsberg.iglu.util.formatting.PatternMatchingSupport;
import org.ijsberg.iglu.util.io.FileSupport;
import org.ijsberg.iglu.util.misc.EncodingSupport;
import org.ijsberg.iglu.util.misc.EncryptionSupport;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 */
public class StandardUserManager implements UserManager, Authenticator, Startable {


	private static final int ITERATIONS = 10 * 1024;
	private static final int SALT_LENGTH = 32;
	private static final int KEY_LENGTH = 256;

	private static String passwordRegex = "\\w{4,10}";
	protected String storageFileName = "./data/users.bin";
	private boolean isStarted = false;

	private HashMap<String, Account> accounts;

	public static String getHash(String password) {

		try {
			byte[] salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(SALT_LENGTH);
			return EncodingSupport.encodeBase64(salt) + "$" + hash(password, salt);
		} catch (NoSuchAlgorithmException e) {
			throw new ConfigurationException("unable to hash password", e);
		} catch (InvalidKeySpecException e) {
			throw new ConfigurationException("unable to hash password", e);
		}
	}

	public static boolean passwordsMatch(String password, String hash) {
		try {
			String[] saltAndPassword = hash.split("\\$");
			if (saltAndPassword.length != 2) {
				return false;
			}
			String hashOfInput = hash(password, EncodingSupport.decodeBase64(saltAndPassword[0]));
			return hashOfInput.equals(saltAndPassword[1]);
		} catch (NoSuchAlgorithmException e) {
			throw new ConfigurationException("unable to hash password", e);
		} catch (InvalidKeySpecException e) {
			throw new ConfigurationException("unable to hash password", e);
		}
	}

	private static String hash(String password, byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
/*		if ("".equals(password)) {
			throw new IllegalArgumentException("empty passwords are not supported");
		}
		if (!PatternMatchingSupport.valueMatchesRegularExpression(password, passwordRegex)) {
			throw new IllegalArgumentException("passwords does not match regular expression '" + passwordRegex + "'");
		}       */
		SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		SecretKey key = f.generateSecret(new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH));
		return EncodingSupport.encodeBase64(key.getEncoded());
	}


	private String getPasswordFromCredentials(Credentials credentials) {
		String password = null;
		if (credentials instanceof SimpleCredentials) {
			password = ((SimpleCredentials) credentials).getPassword();
		}
		return password;
	}


	@Override
	public User authenticate(Credentials credentials) throws AuthenticationException {

		Account account = accounts.get(credentials.getUserId());
		if (account != null) {
			String password = getPasswordFromCredentials(credentials);
			if (passwordsMatch(password, account.getHashedPassword())) {
				BasicUser user = new BasicUser(account.getUserId(), account.getProperties());
				if(account.getProperties().containsKey("group")) {
					user.setGroup(new UserGroup(account.getProperties().getProperty("group"), ""));
				}
				return user;
			}
		}
		System.out.println(new LogEntry("authentication failed for login " + credentials.getUserId()));
		throw new AuthenticationException(AuthenticationException.CREDENTIALS_INVALID);
	}

	@Override
	public User authenticate(Credentials expiredCredentials, Credentials newCredentials) throws AuthenticationException {
		User user = authenticate(expiredCredentials);
		Account account = accounts.get(expiredCredentials.getUserId());
		account.setHashedPassword(getPasswordFromCredentials(newCredentials));
		save();
		return user;
	}

	@Override
	public void start() {
		load();
		isStarted = true;
	}

	@Override
	public boolean isStarted() {
		return isStarted;
	}

	@Override
	public void stop() {
		isStarted = false;
	}


	@Override
	public void addAccount(String userId, String password) {

		Account account = new SimpleAccount(userId, getHash(password));

		accounts.put(userId, account);
		save();
	}

	@Override
	public void removeAccount(String userId) {

		accounts.remove(userId);
		save();
	}

	@Override
	public boolean setGroup(String userId, String groupId) {

		Account account = accounts.get(userId);
		if(account != null) {
			account.putProperty("group", groupId);
			return true;
		}
		return false;
	}

	@Override
	public List<String> listAccounts() {
		List<String> retval = new ArrayList<String>();
		for(Account account : accounts.values()) {
			retval.add(account.getUserId() + ":" + account.getProperties().getProperty("group"));
		}
		return retval;
	}

	@Override
	public void addAccount(User user, String password) {

		Account account = new SimpleAccount(user.getId(), getHash(password), user.getSettings());
		accounts.put(user.getId(), account);
		save();
	}

	@Override
	public void resetPassword(String userId, String oldPassword, String newPassword) {
		Account account = accounts.get(userId);
		if(account != null && passwordsMatch(oldPassword, account.getHashedPassword())) {
			account.setHashedPassword(getHash(newPassword));
			save();
		} else {
			throw new AuthenticationException();
		}
	}

	private void load() {
		try {
			File file = new File(storageFileName);
			if (file.exists()) {
				accounts = (HashMap<String, Account>) FileSupport.readSerializable(storageFileName);
			} else {
				accounts = new HashMap<String, Account>();
				addAccount("admin", "admin");
			}
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException("unable to load account data from '" + storageFileName + "'", e);
		} catch (IOException e) {
			throw new ConfigurationException("unable to load account data from '" + storageFileName + "'", e);
		}
	}

	private void save() {
		try {
			File file = new File(storageFileName);
			if (!file.exists()) {
				FileSupport.createFile(storageFileName);
			}
			FileSupport.saveSerializable(accounts, storageFileName);
		} catch (IOException e) {
			throw new ConfigurationException("unable to save account data to '" + storageFileName + "'", e);
		}
	}


	public void setProperties(Properties properties) {
		storageFileName = properties.getProperty("storage_file_name", storageFileName);
	}


	// ./invoke.sh "core.WebApp.addAccount(username,password)"
}
