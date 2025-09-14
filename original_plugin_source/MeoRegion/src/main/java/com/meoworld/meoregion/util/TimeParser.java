package com.meoworld.meoregion.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {
    private static final Pattern PATTERN = Pattern.compile("(\\d+)([dhms])");

    public static long parseDuration(String durationStr) {
        if (durationStr == null) return 0;
        Matcher matcher = PATTERN.matcher(durationStr.toLowerCase());
        long totalMillis = 0;
        while (matcher.find()) {
            try {
                int value = Integer.parseInt(matcher.group(1));
                String unit = matcher.group(2);
                switch (unit) {
                    case "d": totalMillis += value * 86400000L; break;
                    case "h": totalMillis += value * 3600000L; break;
                    case "m": totalMillis += value * 60000L; break;
                    case "s": totalMillis += value * 1000L; break;
                }
            } catch (NumberFormatException ignored) {}
        }
        return totalMillis;
    }
}

