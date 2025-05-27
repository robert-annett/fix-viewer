package com.rannett.fixplugin.dictionary;

import com.intellij.openapi.project.Project;
import com.rannett.fixplugin.settings.FixViewerSettingsState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FixDictionaryCache {

    private static final Map<String, FixTagDictionary> cache = new ConcurrentHashMap<>();

    // Project-level cache access
    public static FixTagDictionary getDictionary(Project project, String version) {
        return cache.computeIfAbsent(version, v -> loadDictionary(project, v));
    }

    private static FixTagDictionary loadDictionary(Project project, String fixVersion) {
        FixViewerSettingsState settings = FixViewerSettingsState.getInstance(project);

        String customPath = settings.getCustomDictionaryPath(fixVersion);
        if (customPath != null && !customPath.isEmpty()) {
            try {
                return FixTagDictionary.fromFile(new java.io.File(customPath));
            } catch (Exception e) {
                //System.err.println("Failed to load custom dictionary for version " + fixVersion + ": " + e.getMessage());
            }
        }

        return FixTagDictionary.fromBuiltInVersion(fixVersion);
    }

    public static void clearCache() {
        cache.clear();
    }
}
