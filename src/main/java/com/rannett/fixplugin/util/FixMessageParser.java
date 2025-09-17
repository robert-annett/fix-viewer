package com.rannett.fixplugin.util;

import com.intellij.openapi.project.Project;
import com.rannett.fixplugin.settings.FixViewerSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.FieldMap;
import quickfix.FieldNotFound;
import quickfix.Message;

import java.io.File;
import java.io.InputStream;

/**
 * Utility methods for parsing FIX messages with QuickFIX/J.
 */
public final class FixMessageParser {

    private FixMessageParser() {
    }

    /**
     * Load a {@link DataDictionary} for the given FIX version. If a project is provided,
     * its settings will be consulted for custom dictionary paths. Otherwise, only the
     * built-in dictionaries are used.
     */
    public static DataDictionary loadDataDictionary(@NotNull String version, @Nullable Project project) {
        String customPath = null;
        if (project != null) {
            FixViewerSettingsState settings = FixViewerSettingsState.getInstance(project);
            customPath = settings.getCustomDictionaryPath(version);
        }

        try {
            if (customPath != null && !customPath.isEmpty()) {
                return new DataDictionary(new File(customPath).getAbsolutePath());
            }
            String resourcePath = "/dictionaries/" + version + ".xml";
            InputStream stream = FixMessageParser.class.getResourceAsStream(resourcePath);
            if (stream != null) {
                return new DataDictionary(stream);
            }
            return new DataDictionary(version);
        } catch (ConfigError e) {
            // Fallback to FIXT.1.1 if we fail to load the requested dictionary
            if (!"FIXT.1.1".equals(version)) {
                try {
                    return loadDataDictionary("FIXT.1.1", project);
                } catch (Exception ignored) {
                    // ignore and fall through
                }
            }
            // Last resort: attempt to load the default dictionary again without throwing
            try {
                return new DataDictionary("FIXT.1.1");
            } catch (ConfigError ignored2) {
                return null;
            }
        }
    }

    /**
     * Parse a FIX message using the provided dictionary, automatically normalising
     * delimiters. Pipe characters ('|') will be converted to the SOH delimiter so
     * QuickFIX/J can parse them correctly.
     */
    public static Message parse(String message, DataDictionary dd) throws Exception {
        message = message.strip();
        if (!message.contains("\u0001") && message.contains("|")) {
            message = message.replace('|', '\u0001');
        }
        Message qfMsg = new Message();
        qfMsg.fromString(message, dd, false);
        return qfMsg;
    }

    /**
     * Build a descriptive label for the given message using common header fields.
     */
    public static String buildMessageLabel(Message msg, DataDictionary dd) {
        String typeCode = getFieldSafe(msg.getHeader(), 35);
        String typeName = typeCode != null ? dd.getValueName(35, typeCode) : null;
        String sender = getFieldSafe(msg.getHeader(), 49);
        String target = getFieldSafe(msg.getHeader(), 56);
        String seqNum = getFieldSafe(msg.getHeader(), 34);
        String sendTime = getFieldSafe(msg.getHeader(), 52);

        StringBuilder label = new StringBuilder();
        if (typeName != null) label.append(typeName);
        else if (typeCode != null) label.append(typeCode);
        else label.append("Message");

        if (sender != null || target != null) {
            label.append(" ");
            label.append(sender != null ? sender : "?");
            label.append("->");
            label.append(target != null ? target : "?");
        }
        if (seqNum != null || sendTime != null) {
            label.append(" [");
            if (seqNum != null) {
                label.append("Seq ").append(seqNum);
            }
            if (sendTime != null) {
                if (seqNum != null) label.append(" ");
                label.append(sendTime);
            }
            label.append("]");
        }
        return label.toString();
    }

    private static String getFieldSafe(FieldMap map, int tag) {
        if (map.isSetField(tag)) {
            try {
                return map.getString(tag);
            } catch (FieldNotFound ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * Split a block of text into individual FIX messages. Newline characters
     * occurring within length-based DATA fields (e.g. {@code 212/213},
     * {@code 350/351}) are preserved and do not terminate a message.
     * <p>
     * Messages are detected by scanning for the {@code 8=} BeginString tag and
     * continue until the checksum field ({@code 10=###}) delimiter. Any text
     * before the first {@code 8=} or between messages is treated as a separate
     * entry, allowing comment lines to be preserved.
     *
     * @param text raw text potentially containing multiple messages
     * @return list of extracted message strings in order of appearance
     */
    public static java.util.List<String> splitMessages(@NotNull String text) {
        java.util.List<String> messages = new java.util.ArrayList<>();
        if (text.isEmpty()) {
            return messages;
        }

        int index = 0;
        while (index < text.length()) {
            // Skip leading newline characters
            while (index < text.length() && (text.charAt(index) == '\n' || text.charAt(index) == '\r')) {
                index++;
            }
            if (index >= text.length()) {
                break;
            }

            // Treat comment lines as standalone entries
            if (text.charAt(index) == '#') {
                int nl = text.indexOf('\n', index);
                if (nl == -1) {
                    nl = text.length();
                }
                messages.add(text.substring(index, nl));
                index = nl + 1;
                continue;
            }

            int start = text.indexOf("8=", index);
            if (start == -1) {
                messages.add(text.substring(index).trim());
                break;
            }

            if (start > index) {
                String pre = text.substring(index, start).trim();
                if (!pre.isEmpty()) {
                    messages.add(pre);
                }
                index = start;
            }

            int checksumIndex = -1;
            int digitsEnd = -1;
            int searchPosition = start;
            while (true) {
                int candidate = text.indexOf("10=", searchPosition);
                if (candidate == -1) {
                    break;
                }
                if (candidate > start) {
                    char previous = text.charAt(candidate - 1);
                    if (!isMessageBoundaryCharacter(previous)) {
                        searchPosition = candidate + 3;
                        continue;
                    }
                }

                int candidateDigitsStart = candidate + 3;
                int candidateDigitsEnd = candidateDigitsStart;
                while (candidateDigitsEnd < text.length() && Character.isDigit(text.charAt(candidateDigitsEnd))) {
                    candidateDigitsEnd++;
                }
                if (candidateDigitsEnd == candidateDigitsStart) {
                    searchPosition = candidate + 3;
                    continue;
                }

                if (candidateDigitsEnd < text.length()) {
                    char afterDigits = text.charAt(candidateDigitsEnd);
                    if (!isMessageBoundaryCharacter(afterDigits)) {
                        searchPosition = candidate + 3;
                        continue;
                    }
                }

                checksumIndex = candidate;
                digitsEnd = candidateDigitsEnd;
                break;
            }

            if (checksumIndex == -1) {
                int nl = text.indexOf('\n', start);
                if (nl == -1) {
                    messages.add(text.substring(start).trim());
                    break;
                } else {
                    messages.add(text.substring(start, nl).trim());
                    index = nl + 1;
                    continue;
                }
            }

            int msgEnd = digitsEnd;
            if (msgEnd < text.length() && isFieldDelimiter(text.charAt(msgEnd))) {
                msgEnd++;
            }
            messages.add(text.substring(start, msgEnd));
            index = msgEnd;
            while (index < text.length() && (text.charAt(index) == '\n' || text.charAt(index) == '\r')) {
                index++;
            }
        }

        return messages;
    }

    private static boolean isMessageBoundaryCharacter(char character) {
        return isFieldDelimiter(character) || character == '\n' || character == '\r';
    }

    private static boolean isFieldDelimiter(char character) {
        return character == '\u0001' || character == '|';
    }
}
