package com.rannett.fixplugin.util;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.rannett.fixplugin.dictionary.FixTagDictionary;
import com.rannett.fixplugin.settings.FixViewerSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.nio.file.Path;

/**
 * Utility methods for describing and navigating FIX dictionaries.
 */
public final class DictionaryNavigationHelper {

    private DictionaryNavigationHelper() {
    }

    /**
     * Opens the active FIX dictionary for the provided version and navigates to the requested tag/value definition.
     *
     * @param project    current project
     * @param fixVersion FIX version string such as {@code "FIX.4.4"}
     * @param tag        numeric tag to locate
     * @param value      optional enumerated value to locate beneath the tag definition
     */
    public static void jumpToDictionary(@NotNull Project project, @Nullable String fixVersion, @NotNull String tag, @Nullable String value) {
        if (fixVersion == null || fixVersion.isBlank()) {
            Messages.showWarningDialog(project, "Unable to determine FIX version for the selected field.", "Jump to Dictionary");
            return;
        }

        VirtualFile dictionaryFile = findDictionaryFile(project, fixVersion);
        if (dictionaryFile == null) {
            Messages.showErrorDialog(project, "Dictionary file could not be located for version " + fixVersion + ".", "Jump to Dictionary");
            return;
        }

        try {
            String content = VfsUtilCore.loadText(dictionaryFile);
            int offset = findDictionaryOffset(content, tag, value);
            FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, dictionaryFile, Math.max(offset, 0)), true);
        } catch (Exception e) {
            Messages.showErrorDialog(project, "Failed to open dictionary: " + e.getMessage(), "Jump to Dictionary");
        }
    }

    /**
     * Builds a user-facing label describing the active dictionary source.
     *
     * @param project    current project
     * @param fixVersion FIX version string such as {@code "FIX.4.4"}
     * @return label describing whether a built-in or custom dictionary is active
     */
    public static String buildDictionaryLabelText(@NotNull Project project, @Nullable String fixVersion) {
        if (fixVersion == null || fixVersion.isBlank()) {
            return "Active Dictionary: Unknown";
        }

        FixViewerSettingsState settingsState = FixViewerSettingsState.getInstance(project);
        String customPath = settingsState.getCustomDictionaryPath(fixVersion);
        if (customPath != null && !customPath.isBlank()) {
            return "Active Dictionary: User Override - " + Path.of(customPath).getFileName();
        }

        return "Active Dictionary: Built-In - " + fixVersion;
    }

    private static VirtualFile findDictionaryFile(@NotNull Project project, @NotNull String fixVersion) {
        FixViewerSettingsState settingsState = FixViewerSettingsState.getInstance(project);
        String customPath = settingsState.getCustomDictionaryPath(fixVersion);
        if (customPath != null && !customPath.isBlank()) {
            String absolutePath = Path.of(customPath).toAbsolutePath().toString();
            VirtualFile customFile = LocalFileSystem.getInstance().findFileByPath(absolutePath);
            if (customFile != null) {
                return customFile;
            }
        }

        String resourcePath = "/dictionaries/" + fixVersion + ".xml";
        URL resource = FixTagDictionary.class.getResource(resourcePath);
        if (resource != null) {
            return VirtualFileManager.getInstance().findFileByUrl(resource.toExternalForm());
        }

        return null;
    }

    private static int findDictionaryOffset(@NotNull String content, @NotNull String tag, @Nullable String value) {
        String fieldAnchor = "<field number=\"" + tag + "\"";
        int fieldIndex = content.indexOf(fieldAnchor);
        if (fieldIndex < 0) {
            return 0;
        }

        if (value == null || value.isBlank()) {
            return fieldIndex;
        }

        String valueAnchor = "<value enum=\"" + value + "\"";
        int valueIndex = content.indexOf(valueAnchor, fieldIndex);
        return valueIndex >= 0 ? valueIndex : fieldIndex;
    }
}

