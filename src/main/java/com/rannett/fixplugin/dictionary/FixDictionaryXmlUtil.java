package com.rannett.fixplugin.dictionary;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.regex.Pattern;

public final class FixDictionaryXmlUtil {
    private static final Pattern ROOT_PATTERN = Pattern.compile("^\\s*(?:<\\?xml[^>]*>\\s*)?<\\s*fix(?:\\s|>)", Pattern.CASE_INSENSITIVE);

    private FixDictionaryXmlUtil() {
    }

    /**
     * Checks whether the supplied XML file has the shape of a FIX dictionary.
     *
     * @param file candidate XML file
     * @return {@code true} when the file appears to be a FIX dictionary
     */
    public static boolean isFixDictionaryFile(@NotNull VirtualFile file) {
        if (!"xml".equalsIgnoreCase(file.getExtension())) {
            return false;
        }
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document != null) {
            return isFixDictionaryText(document.getText());
        }
        try {
            return isFixDictionaryText(VfsUtilCore.loadText(file));
        } catch (IOException exception) {
            return false;
        }
    }

    /**
     * Checks whether the supplied XML text has the shape of a FIX dictionary.
     *
     * @param text candidate XML text
     * @return {@code true} when the text appears to be a FIX dictionary
     */
    public static boolean isFixDictionaryText(@NotNull String text) {
        if (!ROOT_PATTERN.matcher(text).find()) {
            return false;
        }

        long sectionsFound = Pattern.compile("<\\s*(messages|fields|components|header|trailer)(?:\\s|>)", Pattern.CASE_INSENSITIVE)
                .matcher(text)
                .results()
                .map(matchResult -> matchResult.group(1).toLowerCase())
                .distinct()
                .count();

        return sectionsFound >= 2;
    }
}
