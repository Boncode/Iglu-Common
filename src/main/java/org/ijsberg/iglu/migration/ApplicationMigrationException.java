package org.ijsberg.iglu.migration;

public class ApplicationMigrationException extends RuntimeException {

    public ApplicationMigrationException(String message) {
        super(message);
    }

    public ApplicationMigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
