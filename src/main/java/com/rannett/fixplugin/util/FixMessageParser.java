package com.rannett.fixplugin.util;

import com.intellij.openapi.project.Project;
import com.rannett.fixplugin.settings.FixViewerSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.Message;

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
            throw new RuntimeException("Failed to load dictionary for " + version, e);
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
}
