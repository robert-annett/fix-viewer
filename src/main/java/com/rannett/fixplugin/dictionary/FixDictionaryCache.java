package com.rannett.fixplugin.dictionary;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.rannett.fixplugin.settings.FixViewerSettingsState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(Service.Level.PROJECT)
public final class FixDictionaryCache {

    private final Map<String, FixTagDictionary> cache = new ConcurrentHashMap<>();

    private final Project project;

    public FixDictionaryCache(Project project) {
        this.project = project;
    }

    // Project-level cache access
    public FixTagDictionary getDictionary(String version) {
        return cache.computeIfAbsent(version, v -> loadDictionary(project, v));
    }

    private FixTagDictionary loadDictionary(Project project, String fixVersion) {
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
    
}
