package org.ijsberg.iglu.persistence.json;

import org.ijsberg.iglu.util.ResourceException;

import java.lang.reflect.Field;

public class JsonPersistenceHelper {

    static <T> boolean fieldNameMatchesValue(T entity, String fieldName, Object fieldValue) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(entity);
            if ((value == null && fieldValue == null) || (value != null && value.equals(fieldValue))) {
                return true;
            }
        } catch (ReflectiveOperationException e) {
            throw new ResourceException("Unable to access field " + fieldName, e);
        }
        return false;
    }

    static void setEntityId(Long id, String entityIdName, Object entity) {
        try {
            Field field = entity.getClass().getDeclaredField(entityIdName);
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new ResourceException("cannot set entity id", e);
        }
    }

    static Long getId(String idName, Object entity) {
        return (Long)getFieldValue(idName, entity);
    }

    static Object getFieldValue(String fieldName, Object entity) {
        try{
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(entity);
        } catch (ReflectiveOperationException e) {
            throw new ResourceException("cannot set entity id", e);
        }
    }
}
