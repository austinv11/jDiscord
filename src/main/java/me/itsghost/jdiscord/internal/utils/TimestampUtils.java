package me.itsghost.jdiscord.internal.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampUtils {
    private static final SimpleDateFormat FORMATTER;

    static {
        // 2015-10-07T20:12:45.743000+00:00
        FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    }

    public static Date parse(String date) throws Exception {
        if (date == null || date.equals("null") || date.isEmpty()) return null;
        return FORMATTER.parse(date.substring(0, 23) + date.substring(26));
    }
}
