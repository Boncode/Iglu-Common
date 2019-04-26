package org.ijsberg.iglu;

import org.ijsberg.iglu.configuration.Startable;

public interface Application extends Startable {
    boolean isRunning();
}
