package org.ijsberg.iglu.persistence.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.persistence.PersistenceHelper;
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
//            entity.setId(id); //BasicPersistable T todo
            PersistenceHelper.setEntityId(id, "id", entity);
            repository.put(id, entity);
            save();
            return entity;
        }
    }

    public T insert(long id, T entity) {
        synchronized (lock) {
            assertUniqueAttributes(entity, id);
            repository.put(id, entity);
            save();
            if(id > currentId) {
                currentId = id;
            }
            return entity;
        }
    }

    private void assertUniqueAttributes(T entity, long id) {
        for(String uniqueAttributeName : uniqueAttributeNames) {
            Object fieldValue = PersistenceHelper.getFieldValue(uniqueAttributeName, entity);
            List<T> existingEntityList = readByField(uniqueAttributeName, fieldValue);
            if(!existingEntityList.isEmpty()) {
                if(existingEntityList.size() > 1) {
                    throw new ResourceException("multiple entities with the same unique attribute " + uniqueAttributeName + " found");
                }
//                T existingEntity = existingEntityList.get(0);
//                if(existingEntity.getId() != id) { //todo BasicPersistable T
                if(PersistenceHelper.getId("id", entity) != id) {
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

    public T read(Long id) {
        synchronized (lock) {
            return repository.get(id);
        }
    }

    public List<T> readByField(String fieldName, Object fieldValue) {
        List<T> result = new ArrayList<>();
        for(T entity : repository.values()) {
            if(JsonPersistenceHelper.fieldNameMatchesValue(entity, fieldName, fieldValue)) {
                result.add(entity);
            }
        }
        return result;
    }

    public void update(T entity) {
        synchronized (lock) {
//            Long id = entity.getId(); //todo BasicPersistable T
            Long id = PersistenceHelper.getId("id", entity);
            if(repository.containsKey(id)) {
                assertUniqueAttributes(entity, id);
                repository.put(id, entity);
                save();
            }
            // todo maybe else throw
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

    private void save() { //todo
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
        return new ArrayList<>(repository.values()); //todo probably just use a Collection in this chain
    }
}
