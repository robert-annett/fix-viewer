package com.rannett.fixplugin.ui;

import java.util.ArrayList;
import java.util.List;
import quickfix.field.EncodedSecurityDesc;
import quickfix.field.EncodedSecurityDescLen;
import quickfix.field.XmlData;
import quickfix.field.XmlDataLen;

final class FixMessageFieldParser {

    record TagValue(String tag, String value) {
    }

    List<TagValue> parseMessage(String message) {
        List<TagValue> result = new ArrayList<>();
        int index = 0;
        int expectedLength = -1;
        String dataTag = null;

        while (index < message.length()) {
            int eq = message.indexOf('=', index);
            if (eq == -1) {
                break;
            }

            String tag = message.substring(index, eq);
            index = eq + 1;

            if (dataTag != null && tag.equals(dataTag) && expectedLength >= 0) {
                int end = Math.min(index + expectedLength, message.length());
                String value = message.substring(index, end);
                result.add(new TagValue(tag, value));

                index = end;
                while (index < message.length() && Character.isWhitespace(message.charAt(index))) {
                    index++;
                }
                if (index < message.length() && (message.charAt(index) == '|' || message.charAt(index) == '\u0001')) {
                    index++;
                }

                dataTag = null;
                expectedLength = -1;
                continue;
            }

            int delimPos = findDelimiter(message, index);
            String value = message.substring(index, delimPos);
            result.add(new TagValue(tag, value));
            index = delimPos + 1;

            if (String.valueOf(XmlDataLen.FIELD).equals(tag)) {
                dataTag = parseLength(value, XmlData.FIELD);
                expectedLength = dataTag != null ? Integer.parseInt(value) : -1;
            } else if (String.valueOf(EncodedSecurityDescLen.FIELD).equals(tag)) {
                dataTag = parseLength(value, EncodedSecurityDesc.FIELD);
                expectedLength = dataTag != null ? Integer.parseInt(value) : -1;
            }
        }

        return result;
    }

    private static String parseLength(String value, int field) {
        try {
            Integer.parseInt(value);
            return String.valueOf(field);
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    private static int findDelimiter(String message, int start) {
        int pipe = message.indexOf('|', start);
        int soh = message.indexOf('\u0001', start);
        int end;
        if (pipe == -1) {
            end = soh;
        } else if (soh == -1) {
            end = pipe;
        } else {
            end = Math.min(pipe, soh);
        }
        if (end == -1) {
            end = message.length();
        }
        return end;
    }
}
