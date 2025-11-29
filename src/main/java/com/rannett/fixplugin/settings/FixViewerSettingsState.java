package com.rannett.fixplugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
@State(
        name = "FixViewerSettings",
        storages = {
                @Storage("FixViewerSettings.xml")
        }
)
public final class FixViewerSettingsState implements PersistentStateComponent<FixViewerSettingsState> {

    private static final List<String> BUILT_IN_VERSIONS = List.of("FIX.4.2", "FIX.4.4", "FIXT.1.1");

    private List<DictionaryEntry> dictionaryEntries = new ArrayList<>();

    public static FixViewerSettingsState getInstance(Project project) {
        return project.getService(FixViewerSettingsState.class);
    }

    public List<DictionaryEntry> getDictionaryEntries() {
        ensureBuiltInDictionariesPresent();
        return dictionaryEntries;
    }

    public void setDictionaryEntries(List<DictionaryEntry> dictionaryEntries) {
        this.dictionaryEntries = new ArrayList<>(dictionaryEntries);
        ensureBuiltInDictionariesPresent();
    }

    public List<DictionaryEntry> getDictionariesForVersion(String fixVersion) {
        ensureBuiltInDictionariesPresent();
        return dictionaryEntries.stream()
                .filter(entry -> entry.getVersion().equals(fixVersion))
                .collect(Collectors.toList());
    }

    public DictionaryEntry getDefaultDictionary(String fixVersion) {
        ensureBuiltInDictionariesPresent();
        return getDictionariesForVersion(fixVersion).stream()
                .filter(entry -> entry.isDefaultDictionary() && !entry.isBuiltIn())
                .findFirst()
                .orElseGet(() -> getDictionariesForVersion(fixVersion).stream()
                        .filter(DictionaryEntry::isDefaultDictionary)
                        .findFirst()
                        .orElseGet(() -> getDictionariesForVersion(fixVersion).stream()
                                .filter(entry -> !entry.isBuiltIn())
                                .findFirst()
                                .orElseGet(() -> getDictionariesForVersion(fixVersion).stream()
                                        .findFirst()
                                        .orElse(null))));
    }

    @Override
    public FixViewerSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull FixViewerSettingsState state) {
        this.dictionaryEntries = new ArrayList<>(state.dictionaryEntries);
        ensureBuiltInDictionariesPresent();
    }

    private void ensureBuiltInDictionariesPresent() {
        if (dictionaryEntries == null) {
            dictionaryEntries = new ArrayList<>();
        }
        Set<String> presentVersions = dictionaryEntries.stream()
                .filter(DictionaryEntry::isBuiltIn)
                .map(DictionaryEntry::getVersion)
                .collect(Collectors.toCollection(HashSet::new));
        for (String version : BUILT_IN_VERSIONS) {
            if (!presentVersions.contains(version)) {
                dictionaryEntries.add(DictionaryEntry.builtIn(version));
            }
        }
        normalizeDefaults();
    }

    private void normalizeDefaults() {
        Map<String, List<DictionaryEntry>> grouped = dictionaryEntries.stream()
                .collect(Collectors.groupingBy(DictionaryEntry::getVersion));
        for (Map.Entry<String, List<DictionaryEntry>> entry : grouped.entrySet()) {
            List<DictionaryEntry> versions = entry.getValue();
            DictionaryEntry preferred = versions.stream()
                    .filter(e -> e.isDefaultDictionary() && !e.isBuiltIn())
                    .findFirst()
                    .orElseGet(() -> versions.stream()
                            .filter(DictionaryEntry::isDefaultDictionary)
                            .findFirst()
                            .orElseGet(() -> versions.stream()
                                    .filter(e -> !e.isBuiltIn())
                                    .findFirst()
                                    .orElseGet(() -> versions.isEmpty() ? null : versions.get(0))));
            for (DictionaryEntry dictionaryEntry : versions) {
                dictionaryEntry.setDefaultDictionary(Objects.equals(dictionaryEntry, preferred));
            }
        }
    }

    public static class DictionaryEntry {
        private String version;
        private String path;
        private boolean builtIn;
        private boolean defaultDictionary;

        public DictionaryEntry() {
        }

        public DictionaryEntry(String version, String path, boolean builtIn, boolean defaultDictionary) {
            this.version = version;
            this.path = path;
            this.builtIn = builtIn;
            this.defaultDictionary = defaultDictionary;
        }

        public static DictionaryEntry builtIn(String version) {
            return new DictionaryEntry(version, null, true, true);
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isBuiltIn() {
            return builtIn;
        }

        public void setBuiltIn(boolean builtIn) {
            this.builtIn = builtIn;
        }

        public boolean isDefaultDictionary() {
            return defaultDictionary;
        }

        public void setDefaultDictionary(boolean defaultDictionary) {
            this.defaultDictionary = defaultDictionary;
        }

        public String getCacheKey() {
            if (builtIn) {
                return "BUILTIN:" + version;
            }
            return "CUSTOM:" + path;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            DictionaryEntry that = (DictionaryEntry) obj;
            return builtIn == that.builtIn
                    && defaultDictionary == that.defaultDictionary
                    && Objects.equals(version, that.version)
                    && Objects.equals(path, that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(version, path, builtIn, defaultDictionary);
        }

        @Override
        public String toString() {
            String type = builtIn ? "Built-in" : "Custom";
            String location = builtIn ? "Bundled" : path;
            return version + " - " + type + " (" + location + ")";
        }
    }
}
