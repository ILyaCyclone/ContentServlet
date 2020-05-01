package ru.unisuite.contentservlet.web;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class HttpDateFormatter {
    private HttpDateFormatter() {
    }

    private static final ZoneId GMT = ZoneId.of("GMT");
    private static final DateTimeFormatter HTTP_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).withZone(GMT);

    // parse cache from org.apache.tomcat.util.http.FastHttpDateFormat
    private static final int DATE_CACHE_CAPACITY = 256;
    private static final Map<String, Long> parseCache = new ConcurrentHashMap<>(DATE_CACHE_CAPACITY);
    private static final Map<Long, String> formatCache = new ConcurrentHashMap<>(DATE_CACHE_CAPACITY);

    public static long parse(String date) {
        Long cached = parseCache.get(date);
        if (cached != null) {
            return cached;
        } else {
            long seconds = Instant.from(HTTP_DATE_FORMATTER.parse(date)).getEpochSecond();
            if (parseCache.size() > DATE_CACHE_CAPACITY) {
                parseCache.clear();
            }
            parseCache.put(date, seconds);
            return seconds;
        }
    }

    public static String format(long date) {
        String cached = formatCache.get(date);
        if (cached != null) {
            return cached;
        } else {
            Instant instant = Instant.ofEpochSecond(date);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, GMT);
            String format = HTTP_DATE_FORMATTER.format(localDateTime);
            if (formatCache.size() > DATE_CACHE_CAPACITY) {
                formatCache.clear();
            }
            formatCache.put(date, format);
            return format;
        }
    }
}
