package org.ijsberg.iglu.util;

public interface StatusMonitor {

    long CRASH_INTERVAL_TIME_MILLIS = 600000;

    boolean hasRecentlyCrashed();

    void registerCrash();
}
