package com.sap.cisp.xhna.data.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class DateUtilTest extends TestCase {
    public static Logger logger = LoggerFactory.getLogger(DateUtilTest.class);

    public DateUtilTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDummy() {
        // just to suppress warning
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testParseDate() throws ParseException {
        // HTTP/1.1 301 Moved Permanently
        String time = "2015-08-07T21:27:48+0000";
        Date date = DateUtil.parseDate(time);
        logger.debug("Date time -> {}", date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        assertEquals("2015-08-08T05:27:48", sdf.format(date));

        String time1 = "2015-08-09T19:51:21.835Z";
        Date date1 = DateUtil.parseDate(time1);
        logger.debug("Date time -> {}", date1);
        assertEquals("2015-08-10T03:51:21", sdf.format(date1));

        String time2 = "2015-08-09T10:31:04.526Z";
        Date date2 = DateUtil.parseDate(time2);
        logger.debug("Date time -> {}", date2);
        assertEquals("2015-08-09T18:31:04", sdf.format(date2));
        SimpleDateFormat tmp = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);
        TimeZone UTC = TimeZone.getTimeZone("UTC");
        tmp.setTimeZone(UTC);
        assertEquals(time2, tmp.format(date2));

        String time3 = "2015-08-10T02:02:23.000Z";
        Date date3 = DateUtil.parseDate(time3);
        logger.debug("Date time -> {}", date3);
        assertEquals("2015-08-10T10:02:23", sdf.format(date3));
        tmp.setTimeZone(UTC);
        assertEquals(time3, tmp.format(date3));
        assertEquals("2015-08-10 10:02:23", sdf1.format(date3));
        
        String time4 = "2015-08-10T02:02:23";
        date3 = DateUtil.parseDate(time4);
        logger.debug("Date time4 -> {}", date3);

    }
}
