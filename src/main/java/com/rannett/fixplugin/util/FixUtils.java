package com.rannett.fixplugin.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FixUtils {

    private static final Pattern FIX_VERSION_PATTERN = Pattern.compile(
            "^(FIX(?:T)?\\.[0-9]+(?:\\.[0-9]+)*)(?:[\\u0001|\\s]|$)");

    private static final Pattern MSG_TYPE_PATTERN = Pattern.compile("(?:^|[\\u0001|])35=([^\\u0001|]+)");

    public static Optional<String> extractFixVersion(String text) {
        int start = text.indexOf("8=FIX");
        if (start == -1) {
            return Optional.empty();
        }

        String candidate = text.substring(start + 2);

        Matcher matcher = FIX_VERSION_PATTERN.matcher(candidate);

        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }

        return Optional.empty();
    }

    /**
     * Extracts the FIX message type (tag 35) from a raw message string.
     *
     * @param text raw FIX message text
     * @return message type value if present
     */
    public static Optional<String> extractMessageType(String text) {
        Matcher matcher = MSG_TYPE_PATTERN.matcher(text);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group(1));
        }
        return Optional.empty();
    }
}
