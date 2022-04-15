package org.ijsberg.iglu.persistence.properties;

import junit.framework.TestCase;
import org.ijsberg.iglu.persistence.BasicEntityPersister;
import org.ijsberg.iglu.util.io.FileSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;


public class PersistenceHelperTest extends TestCase {

   // private static final String FILE_LOCATION = "/tmp/persistence/";
    private static final String[] FIELDS = {"id", "name", "value", "bool"};
    private static final String ENTITY_ID = "id";

    File tmpDir;

    @Before
    public void setUp() throws Exception {
        tmpDir = FileSupport.createTmpDir();
        //profile = AnalysisProfile.getDefaultAnalysisProfile("java");
    }

    @After
    public void tearDown() throws Exception {
        FileSupport.deleteFile(tmpDir);
    }

    @Test
    public void test() throws IllegalAccessException, IOException {

        SomeEntity someEntity1 = new SomeEntity();

        Field[] fields = SomeEntity.class.getDeclaredFields();
        for(Field field : fields) {
            field.setAccessible(true);
            System.out.println(field.getName());
            System.out.println(field.get(someEntity1));
        }

        someEntity1.setName("Hello");
        someEntity1.setBool(false);

        BasicEntityPersister<SomeEntity> persister = new BasicEntityPersister(
                tmpDir.getAbsolutePath(), SomeEntity.class, ENTITY_ID, FIELDS);

        assertEquals(0, someEntity1.getId());
        persister.create(someEntity1);
        assertEquals(1, someEntity1.getId());

        BasicEntityPersister<SomeEntity> persisterB = new BasicEntityPersister(
                tmpDir.getAbsolutePath(), SomeEntity.class, ENTITY_ID, FIELDS);

        SomeEntity persistedEntity = persisterB.read(1);
        assertEquals(1, persistedEntity.getId());
        assertEquals("Hello", persistedEntity.getName());
        assertFalse(persistedEntity.isBool());

        assertEquals(4, persisterB.getSize());

        persistedEntity.setName("BLA");
        persisterB.update(persistedEntity);

        BasicEntityPersister<SomeEntity> persisterC = new BasicEntityPersister(
                tmpDir.getAbsolutePath(), SomeEntity.class, ENTITY_ID, FIELDS);

        SomeEntity persistedEntity2 = persisterC.read(1);
        assertEquals("BLA", persistedEntity2.getName());

        assertEquals(4, persisterC.getSize());

        persisterC.delete(1);
        assertEquals(0, persisterC.getSize());

        BasicEntityPersister<SomeEntity> persisterD = new BasicEntityPersister(
                tmpDir.getAbsolutePath(), SomeEntity.class, ENTITY_ID, FIELDS);

        assertEquals(0, persisterD.getSize());
    }



}