package org.ijsberg.iglu.logging.module;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class RotatingFileLoggerTest {

    @Test
    public void getDateFromLogLine() throws Exception {
        Date date = RotatingFileLogger.getDateFromLogLine("VBS 20191003 06:34:11.227");
        System.out.println(date);
        assertEquals(1570077240000l, date.getTime());
    }
}