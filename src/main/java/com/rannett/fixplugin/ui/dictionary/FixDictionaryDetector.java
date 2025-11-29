package com.rannett.fixplugin.ui.dictionary;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for detecting whether a file is likely to be a FIX dictionary.
 */
public final class FixDictionaryDetector {

    private static final Logger LOG = Logger.getInstance(FixDictionaryDetector.class);

    /**
     * Flag used to force the dictionary viewer even when automatic detection fails.
     */
    public static final Key<Boolean> FORCE_DICTIONARY_VIEWER = Key.create("fix.dictionary.viewer.force");

    private static final int PROBE_LENGTH = 2048;

    private FixDictionaryDetector() {
    }

    /**
     * Determines whether the provided file is probably a FIX dictionary.
     *
     * @param file file to inspect
     * @return {@code true} when the file content or name matches FIX dictionary patterns
     */
    public static boolean isLikelyFixDictionary(VirtualFile file) {
        if (Boolean.TRUE.equals(file.getUserData(FORCE_DICTIONARY_VIEWER))) {
            return true;
        }

        if (file.getExtension() == null) {
            return false;
        }

        String extension = file.getExtension().toLowerCase();
        if (!"xml".equals(extension)) {
            return false;
        }

        if (file.getName().toLowerCase().startsWith("fix")) {
            return true;
        }

        String probe = readProbe(file);
        if (probe.isEmpty()) {
            return false;
        }

        if (probe.contains("<fix")) {
            return true;
        }

        if (probe.contains("<messages") && probe.contains("msgtype")) {
            return true;
        }

        if (probe.contains("<fields") && probe.contains("number\"")) {
            return true;
        }

        return false;
    }

    private static String readProbe(VirtualFile file) {
        try (InputStream inputStream = new BufferedInputStream(file.getInputStream())) {
            byte[] buffer = new byte[PROBE_LENGTH];
            int read = inputStream.read(buffer);
            if (read <= 0) {
                return "";
            }
            return new String(buffer, 0, read, StandardCharsets.UTF_8).toLowerCase();
        } catch (Exception e) {
            LOG.debug("Unable to read dictionary probe from file: " + file.getName(), e);
            return "";
        }
    }
}
