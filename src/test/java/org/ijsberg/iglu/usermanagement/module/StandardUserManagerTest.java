package org.ijsberg.iglu.usermanagement.module;

import org.junit.Test;

public class StandardUserManagerTest {

    @Test
    public void getHash() {
        //dlG5%8sQ
        //51964d7aab1ae29f3db3322a9137a8cf

        System.out.println(StandardUserManager.passwordsMatch("dlG5%8sQ", "51964d7aab1ae29f3db3322a9137a8cf"));
    }
}