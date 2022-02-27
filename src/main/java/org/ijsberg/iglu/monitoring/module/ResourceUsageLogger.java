package org.ijsberg.iglu.monitoring.module;

import com.sun.management.OperatingSystemMXBean;
import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.scheduling.Pageable;

import java.io.File;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import org.ijsberg.iglu.util.formatting.NumberFormatter;

public class ResourceUsageLogger implements Pageable, Startable {

    private boolean started = false;
    private OperatingSystemMXBean osBean;

    public ResourceUsageLogger() {
        osBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
    }

    @Override
    public int getPageIntervalInMinutes() {
        return 5;
    }

    @Override
    public int getPageOffsetInMinutes() {
        return 1;
    }

    @Override
    public void onPageEvent(long officialTime) {

        new File("/").getFreeSpace();
        System.gc();
        System.out.println(new LogEntry(Level.VERBOSE, "Memory use: " + new NumberFormatter('.',',').format((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024), 1) + " Mb"));
        System.out.println(new LogEntry(Level.VERBOSE, "System CPU load: " + new NumberFormatter('.',',').format(osBean.getSystemCpuLoad(), 1)));
        System.out.println(new LogEntry(Level.VERBOSE, "Free space: " + new NumberFormatter('.',',').format((new File("/").getFreeSpace()) / (1024 * 1024)) + " Mb"));
        System.out.println(new LogEntry(Level.VERBOSE, "Free space in repository: " + new NumberFormatter('.',',').format((new File("/repository").getFreeSpace()) / (1024 * 1024)) + " Mb"));
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void stop() {
        started = false;
    }
}
