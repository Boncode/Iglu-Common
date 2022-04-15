package org.ijsberg.iglu.persistence.properties;

import org.ijsberg.iglu.util.collection.ListTreeMap;
import org.ijsberg.iglu.util.properties.IgluProperties;

import java.util.List;
import java.util.Properties;

public class BasicPropertiesPersister {

    private final Object lock = new Object();

    private ListTreeMap<Long, String> repository = new ListTreeMap<>();

    private String[] fieldNames;

    private long currentKey;


    public long create(Properties properties) {
//        synchronized (lock) {
            long nextKey = ++currentKey;
            for (String fieldName : fieldNames) {
                repository.put(currentKey, properties.getProperty(fieldName));
            }
            return nextKey;
//        }
    }

    public IgluProperties read(long key) {
        int index = 0;
        IgluProperties properties = new IgluProperties();
        synchronized (lock) {
            List<String> row = repository.get(key);
            for (String fieldName : fieldNames) {
                String value = row.get(index);
                if(value != null) {
                    properties.setProperty(fieldName, value);
                }
                index++;
            }
        }
        return properties;
    }

    public void update(long key, Properties properties) {
        synchronized (lock) {
            for (String fieldName : fieldNames) {
                repository.put(key, properties.getProperty(fieldName));
            }
        }
    }

    public void delete(long key, Properties properties) {
        synchronized (lock) {
            for (String fieldName : fieldNames) {
                repository.put(key, properties.getProperty(fieldName));
            }
        }
    }
}
