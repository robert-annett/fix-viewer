package com.rannett.fixplugin.util;

import com.intellij.openapi.project.Project;
import com.rannett.fixplugin.settings.FixViewerSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.Message;
import quickfix.FieldMap;
import quickfix.FieldNotFound;

import java.io.File;
import java.io.InputStream;

/**
 * Utility methods for parsing FIX messages with QuickFIX/J.
 */
public final class FixMessageParser {

    private FixMessageParser() {}

    /**
     * Load a {@link DataDictionary} for the given FIX version. If a project is provided,
     * its settings will be consulted for custom dictionary paths. Otherwise only the
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
}
