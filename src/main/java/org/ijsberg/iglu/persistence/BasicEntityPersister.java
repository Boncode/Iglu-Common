package org.ijsberg.iglu.persistence;

import org.ijsberg.iglu.util.ResourceException;
import org.ijsberg.iglu.util.collection.ListTreeMap;
import org.ijsberg.iglu.util.io.FileSupport;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;

public class BasicEntityPersister<T> {

    private final Object lock = new Object();

    private ListTreeMap<Long, Serializable> repository = new ListTreeMap<>();

    private String fileLocation;
    private Class<T> entityType;
    private String[] fieldNames;
    private String idName;

    private long currentKey;

    public BasicEntityPersister(String fileLocation, Class<T> entityType, String idName, String[] fieldNames) {
        this.fileLocation = fileLocation;
        this.entityType = entityType;
        this.idName = idName;
        this.fieldNames = fieldNames;
        load();
    }

    public long create(T entity) {
        synchronized (lock) {
            long nextKey = ++currentKey;
            PersistenceHelper.setEntityId(nextKey, idName, entity);
            repository.put(nextKey, PersistenceHelper.convertToRecord(fieldNames, entity));
            save();
            return nextKey;
        }
    }

    public T read(long key) {
        T entity = instantiateEntity();
        int index = 0;
        synchronized (lock) {
            List row = repository.get(key);
            PersistenceHelper.setEntityId(key, idName, entity);
            PersistenceHelper.populateEntity(row, fieldNames, entity);
        }
        save();
        return entity;
    }


    public void update(T entity) {
        synchronized (lock) {
            long key = PersistenceHelper.getId(idName, entity);
            repository.replaceValues(key, PersistenceHelper.convertToRecord(fieldNames, entity));
        }
        save();
    }

    public void delete(long key) {
        synchronized (lock) {
            repository.removeAll(key);
        }
        save();
    }

    private T instantiateEntity() {
        try {
            return ReflectionSupport.instantiateClass(entityType);
        } catch (InstantiationException e) {
            throw new ResourceException("cannot instatiate entity " + entityType);
        }
    }


    private void load() {
        try {
            FileSupport.createDirectory(fileLocation);
            if(FileSupport.fileExists(fileLocation + "/" + entityType.getSimpleName() + ".bin")) {
                repository = new ListTreeMap((TreeMap)FileSupport.readSerializable(fileLocation + "/" + entityType.getSimpleName() + ".bin"));
                if(repository.keySet().size() > 0) {
                    currentKey = repository.descendingKeySet().first();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new ResourceException("cannot load entities from " + fileLocation + "/" + entityType.getSimpleName() + ".bin", e);
        }
    }

    private void save() {
        try {
            FileSupport.saveSerializable((TreeMap)repository.getMap(), fileLocation + "/" + entityType.getSimpleName() + ".bin");
        } catch (IOException e) {
            throw new ResourceException("cannot save entities to " + fileLocation + "/" + entityType.getSimpleName() + ".bin", e);
        }
    }

    public int getSize() {
        return repository.size();
    }
}
