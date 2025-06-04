package com.rannett.fixplugin.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FixUtils {

    public static Optional<String> extractFixVersion(String text) {
        int start = text.indexOf("8=FIX");
        if (start == -1) {
            return Optional.empty();
        }

        String candidate = text.substring(start + 2);

        Matcher matcher = Pattern
                .compile("^(FIX(?:T)?\\.[0-9]+(?:\\.[0-9]+)*)(?:[\\u0001|\\s]|$)")
                .matcher(candidate);

        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }

        return Optional.empty();
    }
}
