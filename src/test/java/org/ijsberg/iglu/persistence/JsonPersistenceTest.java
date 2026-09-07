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

}
