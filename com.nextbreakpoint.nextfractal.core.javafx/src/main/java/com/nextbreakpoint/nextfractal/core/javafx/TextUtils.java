package com.nextbreakpoint.nextfractal.core.javafx;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public static String formatInstant(long millis) {
        return FORMATTER.format(Instant.ofEpochMilli(millis));
    }

    public static String formatInstant(Instant instant) {
        return FORMATTER.format(instant);
    }

    public static String formatDuration(float durationInSeconds) {
        final long duration = (long) Math.ceil(durationInSeconds);
        final long minutes = duration / 60;
        final long seconds = duration % 60;
        if (minutes < 1) {
            return "%d sec".formatted(duration);
        } else {
            return "%d min %d sec".formatted(minutes, seconds);
        }
    }
}
