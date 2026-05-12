package com.rannett.fixplugin.dictionary;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public final class FixDictionaryXmlUtil {
    private static final Pattern ROOT_PATTERN = Pattern.compile("<\\s*fix(?:\\s|>)", Pattern.CASE_INSENSITIVE);

    private FixDictionaryXmlUtil() {
    }

    public static boolean isFixDictionaryFile(@NotNull VirtualFile file) {
        if (!"xml".equalsIgnoreCase(file.getExtension())) {
            return false;
        }
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return false;
        }
        return isFixDictionaryText(document.getText());
    }

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
