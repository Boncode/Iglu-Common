package org.ijsberg.iglu.migration;

import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.logging.module.SimpleFileLogger;
import org.ijsberg.iglu.util.io.FileSupport;
import org.ijsberg.iglu.util.properties.IgluProperties;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.ijsberg.iglu.logging.module.RotatingFileLogger.TIMESTAMP_LOGFILE_FORMAT;

public class ApplicationMigration {

    private static final String LOG_LOCATION = "logs/migration/";

    public static void processMigrations() {
        SimpleFileLogger logger = createLogger();
        logger.start();
        try {
            System.out.println(new LogEntry(Level.VERBOSE, "About to migrate application"));

            List<Migrator> migrators = createMigrators();
            System.out.println(new LogEntry(Level.VERBOSE, migrators.size() + " migration steps have been defined"));
            System.out.println(new LogEntry(Level.VERBOSE, "---------- DRY RUN ----------\n"));
            dryRunMigrators(migrators);

            System.out.println(new LogEntry(Level.VERBOSE, "---------- WET RUN ----------\n"));
            wetRunMigrators(migrators);
        } catch (ApplicationMigrationException e) {
            System.out.println(new LogEntry(Level.CRITICAL, "An error occurred while migrating application", e));
            throw e;
        } finally {
            logger.stop();
        }
    }

    private static SimpleFileLogger createLogger() {
        try {
            FileSupport.createDirectory(LOG_LOCATION);
        } catch (IOException ioException) {
            throw new ApplicationMigrationException("Failed to create directory for migration log file.", ioException);
        }
        return new SimpleFileLogger(getLogFileName());
    }

    private static String getLogFileName() {
        return LOG_LOCATION + "ApplicationMigration" + "." + TIMESTAMP_LOGFILE_FORMAT.format(new Date());
    }

    private static void wetRunMigrators(List<Migrator> migrators) {
        for(Migrator migrator : migrators) {
            try {
                System.out.println(new LogEntry(Level.VERBOSE, "---------- WET RUN for " + migrator.getClass().getSimpleName() +  " ----------\n"));
                migrator.run(false);
            } catch(Throwable t) {
                throw new ApplicationMigrationException("Wet run with migrator: " + migrator.getClass().getSimpleName() + " caused an error.", t);
            }
        }
    }

    private static void dryRunMigrators(List<Migrator> migrators) {
        for(Migrator migrator : migrators) {
            try {
                System.out.println(new LogEntry(Level.VERBOSE, "---------- DRY RUN for " + migrator.getClass().getSimpleName() +  " ----------\n"));
                migrator.run(true);
            } catch(Throwable t) {
                throw new ApplicationMigrationException("Dry run with migrator: " + migrator.getClass().getSimpleName() + " caused an error.", t);
            }
        }
    }

    private static List<Migrator> createMigrators() {
        List<Migrator> migrators = new ArrayList<>();
        if(IgluProperties.propertiesExist("./migrator-properties/migrator.properties")) {
            IgluProperties allMigratorProperties = IgluProperties.loadProperties("./migrator-properties/migrator.properties");
            for(String migratorKey : allMigratorProperties.getSubsectionKeys()) {
                IgluProperties migratorConfig = allMigratorProperties.getSubsection(migratorKey);
                try {
                    migrators.add((Migrator) ReflectionSupport.instantiateClass(migratorConfig.getProperty("class"), migratorConfig.getSubsection("properties")));
                } catch (InstantiationException e) {
                    throw new ApplicationMigrationException("Migrator with class: " + migratorConfig.getProperty("class") + " couldn't be created.", e);
                }
            }
        }
        return migrators;
    }
}
