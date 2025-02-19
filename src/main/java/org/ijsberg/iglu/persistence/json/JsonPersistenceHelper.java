package org.ijsberg.iglu.persistence.json;

import org.ijsberg.iglu.util.ResourceException;

import java.lang.reflect.Field;

public class JsonPersistenceHelper {

    public static <T> boolean fieldNameMatchesValue(T entity, String fieldName, Object fieldValue) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            if (field.get(entity).equals(fieldValue)) {
                return true;
            }
        } catch (ReflectiveOperationException e) {
            throw new ResourceException("Unable to access field " + fieldName, e);
        }
        return false;
    }
}
