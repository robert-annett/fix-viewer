package com.rannett.fixplugin.quickfix;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.io.ByteSequence;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Detects QuickFIX session configuration files based on file content.
 */
public class QuickFixConfigFileTypeDetector implements FileTypeRegistry.FileTypeDetector {

    private static final int MAX_SAMPLE_SIZE = 8192;
    private static final Pattern SECTION_PATTERN = Pattern.compile("(?m)^\\s*\\[(DEFAULT|SESSION)]\\s*$");
    private static final Pattern KEY_PATTERN = Pattern.compile(
            "(?m)^\\s*(BeginString|SenderCompID|TargetCompID|ConnectionType)\\s*="
    );

    /**
     * Attempts to detect a QuickFIX session configuration file.
     *
     * @param file the file to check.
     * @param firstBytes the file content as bytes.
     * @param firstCharsIfText the file content as text if detected as text.
     * @return the QuickFIX config file type if detected; otherwise {@code null}.
     */
    @Nullable
    @Override
    public FileType detect(@NotNull VirtualFile file,
                           @NotNull ByteSequence firstBytes,
                           @Nullable CharSequence firstCharsIfText) {
        if (firstCharsIfText == null || !looksLikeQuickFixConfig(firstCharsIfText)) {
            return null;
        }
        return QuickFixConfigFileType.INSTANCE;
    }

    /**
     * Returns the amount of content needed for detection.
     *
     * @return the desired prefix length.
     */
    @Override
    public int getDesiredContentPrefixLength() {
        return MAX_SAMPLE_SIZE;
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
