package ru.unisuite.contentservlet.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// org.apache.tomcat.util.http.FastHttpDateFormat
// org.springframework.security.web.savedrequest.FastHttpDateFormat
class HttpDateFormatter {
    private static final Logger logger = LoggerFactory.getLogger(HttpDateFormatter.class);

    private HttpDateFormatter() {
    }

    private static final ZoneId GMT = ZoneId.of("GMT");
    private static final DateTimeFormatter HTTP_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).withZone(GMT);

    private static final int DATE_CACHE_CAPACITY = 256;
    private static final Map<String, Long> parseCache = new ConcurrentHashMap<>(DATE_CACHE_CAPACITY);
    private static final Map<Long, String> formatCache = new ConcurrentHashMap<>(DATE_CACHE_CAPACITY);

    public static long parse(String date) {
        Long cached = parseCache.get(date);
        if (cached != null) {
            return cached;
        } else {
            long seconds = -1L; // took default value from tomcat FastHttpDateFormat
            try {
                seconds = Instant.from(HTTP_DATE_FORMATTER.parse(date)).getEpochSecond();
            } catch (DateTimeParseException e) {
                // in tomcat code this exception is ignored
                logger.warn("Unable to parse string to HTTP date format: `{}`", date);
            }
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
