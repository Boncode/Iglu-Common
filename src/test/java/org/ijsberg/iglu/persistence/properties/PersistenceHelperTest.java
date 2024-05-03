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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PersistenceHelperTest extends TestCase {

   // private static final String FILE_LOCATION = "/tmp/persistence/";
    private static final String[] FIELDS = {"id", "name", "value", "bool", "map"};
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

        Map<String, Map<String, String>> projectTimelineProperties = new HashMap<>();
        Map<String, String> singleProjectConfig = new HashMap<>();
        singleProjectConfig.put("stretchTimeline", "true");
        projectTimelineProperties.put("projectName", singleProjectConfig);
        someEntity1.setMap(projectTimelineProperties);

        BasicEntityPersister<SomeEntity> persister = new BasicEntityPersister(
                tmpDir.getAbsolutePath(), SomeEntity.class, ENTITY_ID, FIELDS);

        assertEquals(0, someEntity1.getId());
        persister.create(someEntity1);
        assertTrue(someEntity1.getId() > 0);
        long id = someEntity1.getId();

        BasicEntityPersister<SomeEntity> persisterB = new BasicEntityPersister(
                tmpDir.getAbsolutePath(), SomeEntity.class, ENTITY_ID, FIELDS);

        SomeEntity persistedEntity = persisterB.read(id);
        assertTrue(persistedEntity.getId() > 0);
        assertEquals("Hello", persistedEntity.getName());
        assertFalse(persistedEntity.isBool());

        assertEquals("true", persistedEntity.getMap().get("projectName").get("stretchTimeline"));

        assertEquals(1, persisterB.getSize());

        persistedEntity.setName("BLA");
        persisterB.update(persistedEntity);

        BasicEntityPersister<SomeEntity> persisterC = new BasicEntityPersister(
                tmpDir.getAbsolutePath(), SomeEntity.class, ENTITY_ID, FIELDS);

        SomeEntity persistedEntity2 = persisterC.read(id);
        assertEquals("BLA", persistedEntity2.getName());

        assertEquals(1, persisterC.getSize());

        persisterC.delete(id);
        assertEquals(0, persisterC.getSize());

        BasicEntityPersister<SomeEntity> persisterD = new BasicEntityPersister(
                tmpDir.getAbsolutePath(), SomeEntity.class, ENTITY_ID, FIELDS);

        assertEquals(0, persisterD.getSize());

        persisterD.create(new SomeEntity("hop_1", 20, true));
        persisterD.create(new SomeEntity("hop_2", 20, true));
        persisterD.create(new SomeEntity("hop_3", 30, true));

        List<SomeEntity> entities = persisterD.readByField("name", "hop_1");
        assertEquals(1, entities.size());
        assertEquals("hop_1", entities.get(0).getName());

        entities = persisterD.readByField("value", 20);
        assertEquals(2, entities.size());
    }
}