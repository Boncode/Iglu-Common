package org.ijsberg.iglu.persistence;

import org.ijsberg.iglu.util.ResourceException;
import org.ijsberg.iglu.util.collection.ListMap;
import org.ijsberg.iglu.util.collection.ListTreeMap;
import org.ijsberg.iglu.util.io.FileSupport;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class BasicEntityPersister<T> {

    private final Object lock = new Object();

    private ListTreeMap<Long, Serializable> repository = new ListTreeMap<>();

    private String fileLocation;
    private Class<T> entityType;
    private String[] fieldNames;
    private List<String> fieldNameList;
    private String idName;

    private List<String> uniqueIndexNames = new ArrayList<>();

    private long currentKey;

    public BasicEntityPersister(String fileLocation, Class<T> entityType, String idName, String ... fieldNames) {
        this.fileLocation = fileLocation;
        this.entityType = entityType;
        this.idName = idName;
        this.fieldNames = fieldNames;
        this.fieldNameList = Arrays.asList(fieldNames);
        load();
    }

    public BasicEntityPersister<T> withUniqueIndexOn(String fieldName) {
        uniqueIndexNames.add(fieldName);
        return this;
    }

    public T create(T entity) {
        synchronized (lock) {
//            long nextKey = ++currentKey;
            long nextKey = System.currentTimeMillis();
            if(nextKey <= currentKey) {
                nextKey = currentKey + 1;
            }
            currentKey = nextKey;

            assertUniqueIndexes(entity, nextKey);
            PersistenceHelper.setEntityId(nextKey, idName, entity);
            repository.put(nextKey, PersistenceHelper.convertToRecord(fieldNames, entity));
            save();
            return entity;
        }
    }

    public long insert(long key, T entity) {
        synchronized (lock) {
            assertUniqueIndexes(entity, key);
            PersistenceHelper.setEntityId(key, idName, entity);
            repository.addOrReplaceValues(key, PersistenceHelper.convertToRecord(fieldNames, entity));
            save();
            currentKey = repository.descendingKeySet().first();
            return key;
        }
    }

    private void assertUniqueIndexes(T entity, long currentId) {
        for(String uniqueIndexName : uniqueIndexNames) {
            Object fieldValue = PersistenceHelper.getFieldValue(uniqueIndexName, entity);
            List<T> existingEntity = readByField(uniqueIndexName, fieldValue);
            if(!existingEntity.isEmpty()) {
                if(PersistenceHelper.getId(idName, existingEntity.get(0)) != currentId) {
                    throw new ResourceException("value " + fieldValue + " for field " + uniqueIndexName + " must be unique");
                }
            }

        }
    }

    public T read(long key) {
        T entity;
        int index = 0;
        synchronized (lock) {
            List row = repository.get(key);
            if(row == null) {
                return null;
            }
            entity = instantiateEntity();
            PersistenceHelper.setEntityId(key, idName, entity);
            PersistenceHelper.populateEntity(row, fieldNames, entity);
        }
        //save();
        return entity;
    }

    public List<T> readByField(String filterFieldName, Object fieldValue) {
        List<T> result = new ArrayList<>();
        synchronized (lock) {
            for(Long id : repository.keySet()) {
                List row = repository.get(id);
                Object value = row.get(fieldNameList.indexOf(filterFieldName));
                if(fieldValue.equals(value)) {
                    T entity = instantiateEntity();
                    PersistenceHelper.setEntityId(id, idName, entity);
                    PersistenceHelper.populateEntity(row, fieldNames, entity);
                    result.add(entity);
                }
            }
        }
        return result;
    }

    public List<T> readByField(String filterFieldName, Object fieldValue, String sortFieldName) {
        ListMap<Object,T> result = new ListTreeMap<>();
        synchronized (lock) {
            for(Long id : repository.keySet()) {
                List row = repository.get(id);
                Object value = row.get(fieldNameList.indexOf(filterFieldName));
                if(fieldValue.equals(value)) {
                    T entity = instantiateEntity();
                    PersistenceHelper.setEntityId(id, idName, entity);
                    PersistenceHelper.populateEntity(row, fieldNames, entity);
                    Object sortValue = row.get(fieldNameList.indexOf(sortFieldName));
                    if(sortValue == null) {
                        sortValue = "";
                    }
                    result.put(sortValue, entity);
                }
            }
        }
        return result.values();
    }

    public List<T> readAll() {
        List<T> result = new ArrayList<>();
        synchronized (lock) {
            for(Long id : repository.keySet()) {
                List row = repository.get(id);
                T entity = instantiateEntity();
                PersistenceHelper.setEntityId(id, idName, entity);
                PersistenceHelper.populateEntity(row, fieldNames, entity);
                result.add(entity);
            }
        }
        return result;
    }

    public void update(T entity) {
        synchronized (lock) {
            long key = PersistenceHelper.getId(idName, entity);
            assertUniqueIndexes(entity, key);
            repository.addOrReplaceValues(key, PersistenceHelper.convertToRecord(fieldNames, entity));
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
            throw new ResourceException("cannot instantiate entity " + entityType, e);
        }
    }


    private void load() {
        try {
            FileSupport.createDirectory(fileLocation);
            if(FileSupport.fileExists(getFileName())) {
                repository = new ListTreeMap((TreeMap)FileSupport.readSerializable(getFileName()));
                if(repository.keySet().size() > 0) {
                    currentKey = repository.descendingKeySet().first();
                }
            } else {
                //create empty file
                save();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new ResourceException("cannot load entities from " + fileLocation + "/" + entityType.getSimpleName() + ".bin", e);
        }
    }

    private void save() {
        try {
            FileSupport.saveSerializable((TreeMap)repository.getMap(), getFileName());
        } catch (IOException e) {
            throw new ResourceException("cannot save entities to " + fileLocation + "/" + entityType.getSimpleName() + ".bin", e);
        }
    }

    private String getFileName() {
        return fileLocation + "/" + entityType.getSimpleName() + ".bin";
    }

    public int getSize() {
        return repository.keySet().size();
    }
}
