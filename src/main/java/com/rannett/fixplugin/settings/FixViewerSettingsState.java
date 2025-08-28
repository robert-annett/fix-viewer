package com.rannett.fixplugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

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
    private Map<String, String> fieldTypeColors = new HashMap<>();
    private String headerLabelFields = "35,49,56,34,52";

    public static FixViewerSettingsState getInstance(Project project) {
        return project.getService(FixViewerSettingsState.class);
    }

    public Map<String, String> getCustomDictionaryPaths() {
        return customDictionaryPaths;
    }

    public void setCustomDictionaryPaths(Map<String, String> customDictionaryPaths) {
        this.customDictionaryPaths = customDictionaryPaths;
    }

    /**
     * Return map of field type to color hex string.
     */
    public Map<String, String> getFieldTypeColors() {
        return fieldTypeColors;
    }

    /**
     * Set map of field type to color hex string.
     *
     * @param fieldTypeColors map of type names to hex colour values
     */
    public void setFieldTypeColors(Map<String, String> fieldTypeColors) {
        this.fieldTypeColors = fieldTypeColors;
    }

    /**
     * Get comma separated header field list used for message labels.
     */
    public String getHeaderLabelFields() {
        return headerLabelFields;
    }

    /**
     * Set comma separated header field list used for message labels.
     */
    public void setHeaderLabelFields(String headerLabelFields) {
        this.headerLabelFields = headerLabelFields;
    }

    /**
     * Parse the header label field configuration into a list of integers.
     *
     * @return ordered list of tag numbers to include in message labels
     */
    public java.util.List<Integer> getHeaderFieldList() {
        java.util.List<Integer> result = new java.util.ArrayList<>();
        for (String part : headerLabelFields.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                result.add(Integer.parseInt(trimmed));
            } catch (NumberFormatException ignore) {
                // Ignore invalid numbers
            }
        }
        return result;
    }

    /**
     * Retrieve a color object for the given field type if configured.
     *
     * @param fieldType type name from the dictionary
     * @return configured {@link java.awt.Color} or {@code null}
     */
    public java.awt.Color getColorForFieldType(String fieldType) {
        if (fieldType == null) {
            return null;
        }
        String hex = fieldTypeColors.get(fieldType);
        if (hex == null || hex.isEmpty()) {
            return null;
        }
        try {
            return java.awt.Color.decode(hex);
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    public String getCustomDictionaryPath(String fixVersion) {
        return customDictionaryPaths.get(fixVersion);
    }

    @Override
    public FixViewerSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull FixViewerSettingsState state) {
        this.customDictionaryPaths = state.customDictionaryPaths;
        this.fieldTypeColors = state.fieldTypeColors;
        this.headerLabelFields = state.headerLabelFields;
    }
}
