package org.ijsberg.iglu.usermanagement.domain;

import org.ijsberg.iglu.usermanagement.Account;

import java.util.Properties;

public class JsonSimpleAccount implements Account {

    private long id;

    private String userId;
    private String hashedPassword;
    private Properties properties = new Properties();

    public JsonSimpleAccount() {

    }

    public JsonSimpleAccount(long id, String userId, String hashedPassword, Properties properties) {
        this.id = id;
        this.userId = userId;
        this.hashedPassword = hashedPassword;
        this.properties = properties;
    }

    public JsonSimpleAccount(String userId, String hashedPassword, Properties properties) {
        this.userId = userId;
        this.hashedPassword = hashedPassword;
        this.properties = properties;
    }

    public JsonSimpleAccount(String userId, String hashedPassword) {
        this.userId = userId;
        this.hashedPassword = hashedPassword;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getHashedPassword() {
        return hashedPassword;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    @Override
    public void putProperty(String key, String value) {
        properties.put(key, value);
    }

    @Override
    public void removeProperty(String key) {
        properties.remove(key);
    }

    @Override
    public void setProperties(Properties settings) {
        this.properties = settings;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
