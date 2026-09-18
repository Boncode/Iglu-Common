package org.ijsberg.iglu.persistence.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.util.ResourceException;
import org.ijsberg.iglu.util.io.FileSupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//public class BasicJsonPersister<T extends BasicPersistable> { //todo
public class BasicJsonPersister<T> {

    private final Object lock = new Object();

    private final String fileLocation;
    private final Class<T> entityClass;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private List<String> uniqueAttributeNames = new ArrayList<>();

    private Map<Long, T> repository = new HashMap<>();
    private Long currentId = System.currentTimeMillis();

    public BasicJsonPersister(String fileLocation, Class<T> entityClass) {
        this.fileLocation = fileLocation;
        this.entityClass = entityClass;
        load();
    }

    public BasicJsonPersister<T> withUniqueAttributeName(String uniqueAttributeName) {
        this.uniqueAttributeNames.add(uniqueAttributeName);
        return this;
    }

    private void load() {
        try {
            FileSupport.createDirectory(fileLocation);
            if(FileSupport.fileExists(getFileName())) {
                TypeFactory typeFactory = objectMapper.getTypeFactory();
                MapType mapType = typeFactory.constructMapType(HashMap.class, Long.class, entityClass);
                repository = objectMapper.readValue(new File(getFileName()), mapType);
            }
        } catch (IOException e) {
            throw new ResourceException("cannot load entities from " + fileLocation + "/" + entityClass.getSimpleName() + ".json", e);
        }
    }

    public T create(T entity) {
        synchronized (lock) {
            long id = getNextId();
            assertUniqueAttributes(entity, id);
            JsonPersistenceHelper.setEntityId(id, "id", entity);
            repository.put(id, cloneEntity(entity));
            save();
            return entity;
        }
    }

    public T insert(long id, T entity) {
        //TODO test if this doesn't overwrite existing entity
        synchronized (lock) {
            assertUniqueAttributes(entity, id);
            repository.put(id, cloneEntity(entity));
            save();
            if(id > currentId) {
                currentId = id;
            }
            return entity;
        }
    }

    private void assertUniqueAttributes(T entity, long id) {
        for(String uniqueAttributeName : uniqueAttributeNames) {
            Object fieldValue = JsonPersistenceHelper.getFieldValue(uniqueAttributeName, entity);
            List<T> existingEntityList = readByField(uniqueAttributeName, fieldValue);
            if(!existingEntityList.isEmpty()) {
                if(existingEntityList.size() > 1) {
                    //TODO check if this is useful
                    throw new ResourceException("multiple entities with the same unique attribute " + uniqueAttributeName + " found");
                }
                if(JsonPersistenceHelper.getId("id", entity) != id) {
                    throw new ResourceException("value " + fieldValue + " for field " + uniqueAttributeName + " must be unique");
                }
            }
        }
    }

    private Long getNextId() {
        long nextId = System.currentTimeMillis();
        if(nextId <= currentId) {
            nextId = currentId + 1;
        }
        currentId = nextId;
        return nextId;
    }

    public boolean contains(Long id) {
        return repository.containsKey(id);
    }

    public T read(Long id) {
        synchronized (lock) {
            return cloneEntity(repository.get(id));
        }
    }

    public List<T> readByField(String fieldName, Object fieldValue) {
        List<T> result = new ArrayList<>();
        for(T entity : repository.values()) {
            if(JsonPersistenceHelper.fieldNameMatchesValue(entity, fieldName, fieldValue)) {
                result.add(cloneEntity(entity));
            }
        }
        return result;
    }

    private T cloneEntity(T entity) {
        try {
            String string = objectMapper.writeValueAsString(entity);
            return objectMapper.readValue(string, entityClass);
        } catch (JsonProcessingException e) {
            throw new ResourceException("Error cloning entity", e);
        }
    }

    public void update(T entity) {
        synchronized (lock) {
            Long id = JsonPersistenceHelper.getId("id", entity);
            if(repository.containsKey(id)) {
                assertUniqueAttributes(entity, id);
                repository.put(id, cloneEntity(entity));
                save();
            } else {
                throw new IllegalArgumentException("Entity with id " + id + " does not exist");
            }
        }
    }

    public void delete(Long id) {
        synchronized (lock) {
            T item = repository.remove(id);
            if(item != null) {
                save();
            }
        }
    }

    private void save() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(getFileName()), repository);
        } catch (IOException e) {
            System.out.println(new LogEntry(Level.CRITICAL, "Unable to save JSON entities of type " + entityClass.getSimpleName(), e));
        }
    }

    public int size() {
        return repository.size();
    }

    private String getFileName() {
        return fileLocation + "/" + entityClass.getSimpleName() + ".json";
    }

    public List<T> readAll() {
        List<T> result = new ArrayList<>();
        for(T entity : repository.values()) {
            result.add(cloneEntity(entity));
        }
        return result;
    }
}
