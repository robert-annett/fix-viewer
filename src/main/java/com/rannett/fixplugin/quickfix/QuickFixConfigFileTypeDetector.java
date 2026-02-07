package com.rannett.fixplugin.quickfix;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeDetector;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Detects QuickFIX session configuration files based on file content.
 */
public class QuickFixConfigFileTypeDetector implements FileTypeDetector {

    private static final int MAX_SAMPLE_SIZE = 65536;
    private static final Pattern SECTION_PATTERN = Pattern.compile("(?m)^\\s*\\[(DEFAULT|SESSION)]\\s*$");
    private static final Pattern KEY_PATTERN = Pattern.compile(
            "(?m)^\\s*(BeginString|SenderCompID|TargetCompID|ConnectionType)\\s*="
    );

    /**
     * Attempts to detect a QuickFIX session configuration file.
     *
     * @param file the file to check.
     * @param content the file content as bytes.
     * @param fileContent the file content as text.
     * @return the QuickFIX config file type if detected; otherwise {@code null}.
     */
    @Nullable
    @Override
    public FileType detect(@NotNull VirtualFile file, @NotNull byte[] content, @NotNull CharSequence fileContent) {
        if (!looksLikeQuickFixConfig(fileContent)) {
            return null;
        }
        return QuickFixConfigFileType.INSTANCE;
    }

    private boolean looksLikeQuickFixConfig(@NotNull CharSequence fileContent) {
        int length = Math.min(fileContent.length(), MAX_SAMPLE_SIZE);
        String sample = fileContent.subSequence(0, length).toString();
        boolean hasSection = SECTION_PATTERN.matcher(sample).find();
        if (!hasSection) {
            return false;
        }
        return KEY_PATTERN.matcher(sample).find();
    }
}
