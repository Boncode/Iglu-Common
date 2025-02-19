package org.ijsberg.iglu.persistence;

import java.security.SecureRandom;

public class IdGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int ID_LENGTH = 10;

    /***
     * Generates a new alphanumeric id of length ID_LENGTH. Includes characters 0-9, a-z, A-Z.
     * @return a new alphanumeric id.
     */
    public static String newId() {
        return SECURE_RANDOM.ints(48, 122 + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(ID_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
