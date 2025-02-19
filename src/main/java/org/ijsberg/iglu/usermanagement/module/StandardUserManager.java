package org.ijsberg.iglu.usermanagement.module;

import org.ijsberg.iglu.FatalException;
import org.ijsberg.iglu.access.*;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.persistence.json.BasicJsonPersister;
import org.ijsberg.iglu.usermanagement.Account;
import org.ijsberg.iglu.usermanagement.UserManager;
import org.ijsberg.iglu.usermanagement.domain.JsonSimpleAccount;
import org.ijsberg.iglu.util.ResourceException;
import org.ijsberg.iglu.util.misc.EncodingSupport;
import org.ijsberg.iglu.util.properties.IgluProperties;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class StandardUserManager implements UserManager, Authenticator, Startable {


	public static final String PBKDF_2_WITH_HMAC_SHA_1 = "PBKDF2WithHmacSHA1";
	private static final int ITERATIONS = 10 * 1024;
	private static final int SALT_LENGTH = 32;
	private static final int KEY_LENGTH = 256;

	private boolean isStarted = false;

	private BasicJsonPersister<JsonSimpleAccount> accountPersister;

	private final byte[] salt;
	protected String dataDir = "./data/users.bin";

	public StandardUserManager(byte[] salt) {
		this.salt = salt;
	}

	public String getHash(String password) {
		if(salt == null) {
			throw new ResourceException("salt not yet created");
		}
		try {
			return EncodingSupport.encodeBase64(salt) + "$" + hash(password, salt);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
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
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new ConfigurationException("unable to hash password", e);
		}
	}

	private static String hash(String password, byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
		SecretKeyFactory f = SecretKeyFactory.getInstance(PBKDF_2_WITH_HMAC_SHA_1);
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

		Account account = getAccountByUserId(credentials.getUserId());
		if (account != null) {
			String password = getPasswordFromCredentials(credentials);
			if (passwordsMatch(password, account.getHashedPassword())) {
				BasicUser user = new BasicUser(account.getUserId(), IgluProperties.copy(account.getProperties()));
				if(account.getProperties().containsKey("group")) {
					user.addGroup(new UserGroup(account.getProperties().getProperty("group"), ""));
				}
				return user;
			}
		}
		System.out.println(new LogEntry("authentication failed for login " + credentials.getUserId()));
		throw new AuthenticationException(AuthenticationException.CREDENTIALS_INVALID);
	}

	private JsonSimpleAccount getAccountByUserId(String userId) {
		List<JsonSimpleAccount> accounts = accountPersister.readByField("userId", userId);
		if (accounts.isEmpty()) {
			return null;
		}
		return accounts.get(0);
	}

	@Override
	public User authenticate(Credentials expiredCredentials, Credentials newCredentials) throws AuthenticationException {
		User user = authenticate(expiredCredentials);
		JsonSimpleAccount account = getAccountByUserId(expiredCredentials.getUserId());
		account.setHashedPassword(getPasswordFromCredentials(newCredentials));
		accountPersister.update(account);
		return user;
	}

	@Override
	public User getSystemUser(String userId) {
		throw new FatalException("not implemented");
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
		Account account = new JsonSimpleAccount(userId, getHash(password));
		addAccount(account);
	}

	private void addAccount(Account account) {
		accountPersister.create(new JsonSimpleAccount(account.getUserId(), account.getHashedPassword(), account.getProperties()));
	}

	@Override
	public void removeAccount(String userId) {
		JsonSimpleAccount account = getAccountByUserId(userId);
		if(account != null) {
			accountPersister.delete(account.getId());
		}
	}

	@Override
	public boolean setGroup(String userId, String groupId) {
		JsonSimpleAccount account = getAccountByUserId(userId);
		if(account != null) {
			account.putProperty("group", groupId);
			accountPersister.update(account);
			return true;
		}
		return false;
	}

	@Override
	public List<String> listAccounts() {
		List<String> retval = new ArrayList<>();
		for(Account account : accountPersister.readAll()) {
			retval.add(account.getUserId() + ":" + account.getProperties().getProperty("group"));
		}
		return retval;
	}

	public List<JsonSimpleAccount> getAccounts() {
		return accountPersister.readAll();
	}

	@Override
	public void addAccount(User user, String password) {
		accountPersister.create(new JsonSimpleAccount(user.getId(), getHash(password), user.getSettings()));
	}

	@Override
	public void updateAccount(User user) {
		JsonSimpleAccount account = getAccountByUserId(user.getId());
		if(account != null) {
			account.setProperties(user.getSettings());
			accountPersister.update(account);
		}
	}

	@Override
	public void resetPassword(String userId, String newPassword) {
		JsonSimpleAccount account = getAccountByUserId(userId);
		account.setHashedPassword(getHash(newPassword));
		accountPersister.update(account);
	}

	@Override
	public void resetPassword(String userId, String oldPassword, String newPassword) {
		JsonSimpleAccount account = getAccountByUserId(userId);
		if(account != null && passwordsMatch(oldPassword, account.getHashedPassword())) {
			account.setHashedPassword(getHash(newPassword));
			accountPersister.update(account);
		} else {
			throw new AuthenticationException();
		}
	}

	private void load() {
		accountPersister = new BasicJsonPersister<>(dataDir, JsonSimpleAccount.class)
				.withUniqueAttributeName("userId");
		if(accountPersister.readAll().isEmpty()) {
			JsonSimpleAccount admin = new JsonSimpleAccount("admin", getHash("admin"));
			admin.putProperty("passwordChanged", "false");
			admin.putProperty("roles",Roles.getRole(Roles.ADMINISTRATOR).getName());
			addAccount(admin);
		}
	}

	public void setProperties(Properties properties) {
		dataDir = properties.getProperty("data_dir", dataDir);
	}
}
