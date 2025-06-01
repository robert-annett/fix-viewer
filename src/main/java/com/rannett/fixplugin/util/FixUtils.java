package com.rannett.fixplugin.util;

import java.util.Optional;

public class FixUtils {

    public static Optional<String> extractFixVersion(String text) {
        int start = text.indexOf("8=FIX");
        if (start == -1) {
            return Optional.empty();
        }

        int endPipe = text.indexOf('|', start);
        int endSoh = text.indexOf('\u0001', start);
        int end;

        if (endPipe == -1 && endSoh == -1) {
            end = text.length();
        } else if (endPipe == -1) {
            end = endSoh;
        } else if (endSoh == -1) {
            end = endPipe;
        } else {
            end = Math.min(endPipe, endSoh);
        }

        String versionField = text.substring(start + 2, end);
        return Optional.of(versionField);
    }
}
