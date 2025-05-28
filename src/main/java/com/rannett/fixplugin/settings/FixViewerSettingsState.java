package com.rannett.fixplugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Service(Service.Level.PROJECT)
@State(
        name = "FixViewerSettings",
        storages = {
                @Storage("FixViewerSettings.xml")
        }
)
public final class FixViewerSettingsState implements PersistentStateComponent<FixViewerSettingsState> {

    private Map<String, String> customDictionaryPaths = new HashMap<>();

    public static FixViewerSettingsState getInstance(Project project) {
        return project.getService(FixViewerSettingsState.class);
    }

    public Map<String, String> getCustomDictionaryPaths() {
        return customDictionaryPaths;
    }

    public void setCustomDictionaryPaths(Map<String, String> customDictionaryPaths) {
        this.customDictionaryPaths = customDictionaryPaths;
    }

    public String getCustomDictionaryPath(String fixVersion) {
        return customDictionaryPaths.get(fixVersion);
    }

    public void setCustomDictionaryPath(String fixVersion, String path) {
        customDictionaryPaths.put(fixVersion, path);
    }

    @Override
    public @Nullable FixViewerSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull FixViewerSettingsState state) {
        this.customDictionaryPaths = state.customDictionaryPaths;
    }
}
