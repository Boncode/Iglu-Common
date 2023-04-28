package org.ijsberg.iglu;

import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.configuration.Startable;

public interface Application extends Startable {
    boolean isRunning();

    /*
            public StandardApplication(Properties properties) {
                IgluProperties igluProperties = IgluProperties.copy(properties);
            }
        */
    Assembly getCoreAssembly();
}
