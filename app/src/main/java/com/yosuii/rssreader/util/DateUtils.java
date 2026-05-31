package com.yosuii.rssreader.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    private static final String[] PATTERNS = {
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm Z",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ssXXX"
    };

    private DateUtils() {
    }

    public static long parseFeedDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0L;
        }
        String text = value.trim();
        for (String pattern : PATTERNS) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                return format.parse(text).getTime();
            } catch (ParseException ignored) {
            }
        }
        return 0L;
    }
}
