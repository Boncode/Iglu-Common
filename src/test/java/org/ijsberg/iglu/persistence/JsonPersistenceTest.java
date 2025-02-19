package org.ijsberg.iglu.persistence;

import org.ijsberg.iglu.persistence.json.BasicJsonPersister;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class JsonPersistenceTest {

    @Test
    @Ignore
    public void testJsonPersistence() {
        String fileLocation = "C:\\Users\\knaar\\Desktop\\";
        long timeStart = System.currentTimeMillis();
        BasicJsonPersister<TestDto> jsonPersister = new BasicJsonPersister<>(fileLocation, TestDto.class);
        long timeLoading = System.currentTimeMillis();
        System.out.println("Took " + (timeLoading-timeStart) + " ms to load " + jsonPersister.size() + " entities.");
//        for(int i = 0; i < 10000; i++) {
//            jsonPersister.create(new TestDto(
//                    "aParam", 160, new NestedTestDto(
//                            224, "someStringexample"
//                    )
//            ));
//        }
//        System.out.println("Took " + (System.currentTimeMillis() - timeLoading) + " ms to write " + jsonPersister.size() + " entities.");

//        TestDto readById = jsonPersister.read(1739808298016L);
//        System.out.println(readById.getId());
        timeLoading = System.currentTimeMillis();
        List<TestDto> filtered = jsonPersister.readByField("b", 160);
        System.out.println("Took " + (System.currentTimeMillis() -timeLoading) + " ms to load " + filtered.size() + " entities by field value \"b\" equals 188");
//        System.out.println("Took " + (System.currentTimeMillis() -timeLoading) + " ms to read by id in 11100 entities.");

        // NOTE very inefficient write operations! No batching
        // Took 354 ms to load 0 entities.
        // Took 517 ms to write 100 entities.
        // Took 461 ms to load 100 entities.
        // Took 2189 ms to write 1000 entities.
        // Took 486 ms to load 1100 entities.
        // Took 61943 ms to write 11100 entities.
        // Took 655 ms to load 11100 entities.
        // size on disc: 16.5KB for 100 entities scales linear but larger overall
        // size on disc: 170KB for 1100 entities
        // size on disc: 1.7MB for 11100 entities

        // reading by id:
        // Took 18 ms to read by id in 11100 entities. (just a hashmap lookup tbh)

        // filtering 11100 entities based on some field value:
        // Took 47 ms to load 10000 entities by field value "b" equals 180
        // Took 41 ms to load 1100 entities by field value "b" equals 2
    }

    @Test
    @Ignore
    public void testBasicEntityPersistence() throws Exception {
        String fileLocation = "C:\\Users\\knaar\\Desktop\\";
        long timeStart = System.currentTimeMillis();
        BasicEntityPersister<TestDto> entityPersister = new BasicEntityPersister<>(fileLocation, TestDto.class, "id", "a", "b", "nestedTestDto");
        long timeLoading = System.currentTimeMillis();
        System.out.println("Took " + (timeLoading-timeStart) + " ms to load " + entityPersister.getSize() + " entities.");
        for(int i =0; i < 1000; i++) {
            entityPersister.create(new TestDto("aValue", 180, new NestedTestDto(234, "someString")));
        }
        System.out.println("Took " + (System.currentTimeMillis() - timeLoading) + " ms to write " + entityPersister.getSize() + " entities.");
//        TestDto readById = entityPersister.read(1724323834001L);
//        System.out.println(readById.getB());
        List<TestDto> filtered = entityPersister.readByField("b", 180);
        System.out.println("Took " + (System.currentTimeMillis() -timeLoading) + " ms to load " + filtered.size() + " entities by field value \"b\" equals 188");
//        System.out.println("Took " + (System.currentTimeMillis() -timeLoading) + " ms to read by id in 2100 entities.");

        // NOTE very inefficient write operations! No batching
        // Took 69 ms to load 0 entities.
        // Took 501 ms to write 100 entities.
        // Took 164 ms to load 100 entities.
        // Took 27471 ms to write 1100 entities.
        // Took 235 ms to load 1100 entities.
        // Writing 10000 entities won't even finish before the end of time, too inefficient
        // Took 223 ms to load 1100 entities.
        // Took 68341 ms to write 2100 entities. --> because we're checking keys all the time...
        // size on disc: 6.5 KB for 100 entities
        // size on disc: 66.9 KB for 1100 entities pretty much linear
        // for 11100 entities it would probably be 670 KB

        // filtering, reading by id
        // Took 75 ms to read by id in 2100 entities.

        // filtering by field value
        // Took 47 ms to load 1100 entities by field value "b" equals 2
        // Took 60 ms to load 1000 entities by field value "b" equals 180
    }

    @Test
    @Ignore
    public void testMigrateToJsonPersistence() throws Exception {
        String fileLocation = "C:\\Users\\knaar\\Desktop\\";
        long timeStart = System.currentTimeMillis();
        BasicEntityPersister<TestDto> entityPersister = new BasicEntityPersister<>(fileLocation, TestDto.class, "id", "a", "b", "nestedTestDto");
        BasicJsonPersister<TestDto> jsonPersister = new BasicJsonPersister<>(fileLocation, TestDto.class);
        for(TestDto testDto : entityPersister.readAll()) {
            jsonPersister.insert(testDto.getId(), testDto);
        }
        long timeEnd = System.currentTimeMillis();
        System.out.println("Took: " + (timeEnd - timeStart) + " ms to load " + jsonPersister.size() + " entities.");
    }

    @Test
    @Ignore
    public void testMigrationToJsonSuccessful() throws Exception {
        String fileLocation = "C:\\Users\\knaar\\Desktop\\";
        BasicEntityPersister<TestDto> entityPersister = new BasicEntityPersister<>(fileLocation, TestDto.class, "id", "a", "b", "nestedTestDto");
        BasicJsonPersister<TestDto> jsonPersister = new BasicJsonPersister<>(fileLocation, TestDto.class);
        for(TestDto testDto : entityPersister.readAll()) {
            TestDto convertedDto = jsonPersister.read(testDto.getId());
            if(!convertedDto.equals(testDto)) {
                throw new Exception();
            }
        }
    }
}
