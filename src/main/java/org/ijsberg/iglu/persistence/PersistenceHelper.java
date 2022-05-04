package org.ijsberg.iglu.persistence;

import org.ijsberg.iglu.util.ResourceException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PersistenceHelper {

    public static List convertToRecord(String[] fieldNames, Object entity) {
        try {
            List result = new ArrayList();

           // Field[] fields = entity.getClass().getDeclaredFields();
            for(String fieldName : fieldNames) {
                Field field = entity.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                result.add(field.get(entity));
            }
            return result;
        } catch (ReflectiveOperationException e) {
            throw new ResourceException("cannot convert to record", e);
        }
    }

    public static Long getId(String idName, Object entity) {
        return (Long)getFieldValue(idName, entity);
    }

    public static Object getFieldValue(String fieldName, Object entity) {
        try{
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(entity);
        } catch (ReflectiveOperationException e) {
            throw new ResourceException("cannot set entity id", e);
        }
    }

    public static void populateEntity(List values, String[] fieldNames, Object entity) {
        try {
            int index = 0;
            for (String fieldName : fieldNames) {
                if(index < values.size()) {
                    Field field = entity.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = values.get(index);
                    if(value != null) {
                        try {
                            field.set(entity, values.get(index));
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                            //throw e;
                        }
                    }
                }
                index++;
            }
        } catch (ReflectiveOperationException e) {
            throw new ResourceException("cannot set entity id", e);
        }
    }

    public static void setEntityId(Long id, String entityIdName, Object entity) {
        try {
            Field field = entity.getClass().getDeclaredField(entityIdName);
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new ResourceException("cannot set entity id", e);
        }
    }
}
