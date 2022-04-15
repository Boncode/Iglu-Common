package org.ijsberg.iglu.monitoring.module;

import com.sun.management.OperatingSystemMXBean;
import org.junit.Test;

import java.io.File;
import java.lang.management.ManagementFactory;

public class ResourceUsageLoggerTest {

    @Test
    public void onPageEvent() {
        System.out.println(Math.round((double) (new File("/").getFreeSpace()) / (1024 * 1024)));

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        System.out.println("load:" + osBean.getSystemCpuLoad());
    }
}