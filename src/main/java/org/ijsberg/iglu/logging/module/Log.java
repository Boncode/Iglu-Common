package org.ijsberg.iglu.logging.module;

import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;

import java.io.Serializable;

/**
 * Created by jeroe on 21/12/2017.
 */
public class Log {
    public static void log(Throwable data) {
        System.out.println(new LogEntry(data));
    }

    public static void log(String message) {
        System.out.println(new LogEntry(message));
    }

    public static void log(String message, Serializable data) {
        System.out.println(new LogEntry(message, data));
    }

    public static void log(Level level, String message) {
        System.out.println(new LogEntry(level, message));
    }

    public static void log(Level level, String message, Serializable data) {
        System.out.println(new LogEntry(level, message, data));
    }

}
