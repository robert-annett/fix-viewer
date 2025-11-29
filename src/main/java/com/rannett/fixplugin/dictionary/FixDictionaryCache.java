package com.rannett.fixplugin.dictionary;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.rannett.fixplugin.settings.FixViewerSettingsState;
import com.rannett.fixplugin.settings.FixViewerSettingsState.DictionaryEntry;

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
        FixViewerSettingsState settings = FixViewerSettingsState.getInstance(project);
        DictionaryEntry entry = settings.getDefaultDictionary(version);
        return getDictionary(entry, version);
    }

    public FixTagDictionary getDictionary(DictionaryEntry entry, String version) {
        String cacheKey = entry != null ? entry.getCacheKey() : "DEFAULT:" + version;
        return cache.computeIfAbsent(cacheKey, v -> loadDictionary(project, entry, version));
    }

    /**
     * Clears the cached dictionaries so that subsequent lookups reload from disk or bundled resources.
     */
    public void clear() {
        cache.clear();
    }

    private FixTagDictionary loadDictionary(Project project, DictionaryEntry entry, String fixVersion) {
        if (entry != null && !entry.isBuiltIn() && entry.getPath() != null && !entry.getPath().isEmpty()) {
            return FixTagDictionary.fromFile(new java.io.File(entry.getPath()));
        }
        return FixTagDictionary.fromBuiltInVersion(fixVersion);
    }

}
