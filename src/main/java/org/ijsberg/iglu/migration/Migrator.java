package org.ijsberg.iglu.migration;

import java.io.IOException;

public interface Migrator {

    void run(boolean isDryRun) throws IOException;
}
