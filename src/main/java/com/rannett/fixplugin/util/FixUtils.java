package com.rannett.fixplugin.util;

import java.util.Optional;

public class FixUtils {

    public static Optional<String> extractFixVersion(String text) {
        int start = text.indexOf("8=FIX");
        if (start == -1) {
            return Optional.empty();
        }

        String candidate = text.substring(start + 2);

        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^(FIX(?:T)?\\.[0-9]+(?:\\.[0-9]+)*)(?:[\\u0001|\\s]|$)")
                .matcher(candidate);

        if (m.find()) {
            return Optional.of(m.group(1));
        }

        return Optional.empty();
    }
}
