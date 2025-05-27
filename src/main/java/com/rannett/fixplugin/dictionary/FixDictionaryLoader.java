package com.rannett.fixplugin.dictionary;

import com.intellij.openapi.project.Project;
import com.rannett.fixplugin.settings.FixViewerSettingsState;

import java.io.File;

public class FixDictionaryLoader {

    public static FixTagDictionary loadDictionary(Project project, String fixVersion) {
        FixViewerSettingsState settings = FixViewerSettingsState.getInstance(project);

        String customPath = settings.getCustomDictionaryPath(fixVersion);
        if (customPath != null && !customPath.isEmpty()) {
            File customFile = new File(customPath);
            if (customFile.exists() && customFile.isFile()) {
                try {
                    return FixTagDictionary.fromFile(customFile);
                } catch (Exception e) {
                    System.err.println("Failed to load custom dictionary for version " + fixVersion + ": " + e.getMessage());
                }
            }
        }

        // Fallback to built-in dictionary
        return FixTagDictionary.fromBuiltInVersion(fixVersion);
    }
}
