package com.rannett.fixplugin.quickfix;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Documentation provider for QuickFIX session configuration files.
 */
public class QuickFixConfigDocumentationProvider extends AbstractDocumentationProvider {

    private static final String RESOURCE_PATH = "/documentation/quickfix-session-config.tsv";
    private static final Key<String> CONFIG_KEY = Key.create("QUICKFIX_CONFIG_KEY");
    private static final Map<String, QuickFixConfigSetting> SETTINGS = loadSettings();

    /**
     * Returns the element to use for QuickFIX configuration documentation.
     *
     * @param editor the editor instance.
     * @param file the file containing the element.
     * @param contextElement the element under the caret.
     * @param offset the caret offset.
     * @return the element to use for documentation, or null when none is available.
     */
    @Override
    public @Nullable PsiElement getCustomDocumentationElement(
            @NotNull Editor editor,
            @NotNull PsiFile file,
            @Nullable PsiElement contextElement,
            int offset
    ) {
        if (contextElement == null) {
            return null;
        }

        if (!file.getLanguage().isKindOf(QuickFixConfigLanguage.INSTANCE)) {
            return null;
        }

        String key = findKeyAtOffset(file.getText(), offset);
        if (key == null) {
            return null;
        }

        contextElement.putUserData(CONFIG_KEY, key);
        return contextElement;
    }

    /**
     * Generates documentation HTML for a QuickFIX configuration key.
     *
     * @param element the element to document.
     * @param originalElement the original element under the caret.
     * @return HTML content, or null when no documentation is available.
     */
    @Override
    public @Nullable String generateDoc(@Nullable PsiElement element, @Nullable PsiElement originalElement) {
        PsiElement targetElement = element != null ? element : originalElement;
        if (targetElement == null) {
            return null;
        }

        PsiFile file = targetElement.getContainingFile();
        if (file == null || !file.getLanguage().isKindOf(QuickFixConfigLanguage.INSTANCE)) {
            return null;
        }

        String key = Optional.ofNullable(targetElement.getUserData(CONFIG_KEY))
                .orElseGet(() -> findKeyAtOffset(file.getText(), targetElement.getTextOffset()));
        if (key == null) {
            return null;
        }

        QuickFixConfigSetting setting = SETTINGS.get(key);
        if (setting == null) {
            return null;
        }

        StringBuilder doc = new StringBuilder("<html><body>");
        doc.append("<b>").append(setting.key()).append("</b>");
        doc.append("<br/>").append(setting.description());
        if (!setting.values().isEmpty()) {
            doc.append("<br/><b>Values</b>: ").append(setting.values());
        }
        if (!setting.defaultValue().isEmpty()) {
            doc.append("<br/><b>Default</b>: ").append(setting.defaultValue());
        }
        doc.append("</body></html>");
        return doc.toString();
    }

    private static String findKeyAtOffset(String text, int offset) {
        if (text == null || text.isEmpty() || offset < 0) {
            return null;
        }

        int boundedOffset = Math.min(offset, text.length() - 1);
        int lineStart = text.lastIndexOf('\n', boundedOffset);
        lineStart = lineStart == -1 ? 0 : lineStart + 1;
        int lineEnd = text.indexOf('\n', boundedOffset);
        lineEnd = lineEnd == -1 ? text.length() : lineEnd;

        String line = text.substring(lineStart, lineEnd);
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        if (trimmed.startsWith("[") || isComment(trimmed)) {
            return null;
        }

        int equalsIndex = line.indexOf('=');
        if (equalsIndex < 0) {
            return null;
        }

        String key = line.substring(0, equalsIndex).trim();
        if (key.isEmpty()) {
            return null;
        }

        return key;
    }

    private static boolean isComment(String trimmedLine) {
        if (trimmedLine.startsWith("#") || trimmedLine.startsWith(";")) {
            return true;
        }
        return trimmedLine.startsWith("//");
    }

    private static Map<String, QuickFixConfigSetting> loadSettings() {
        try (InputStream stream = QuickFixConfigDocumentationProvider.class.getResourceAsStream(RESOURCE_PATH)) {
            if (stream == null) {
                return Map.of();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(line -> line.split("\t", -1))
                        .filter(parts -> parts.length >= 2)
                        .map(QuickFixConfigDocumentationProvider::toSetting)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(
                                QuickFixConfigSetting::key,
                                setting -> setting,
                                (first, second) -> first,
                                LinkedHashMap::new
                        ));
            }
        } catch (IOException exception) {
            return Map.of();
        }
    }

    private static QuickFixConfigSetting toSetting(String[] parts) {
        String key = parts[0].trim();
        if (key.isEmpty()) {
            return null;
        }
        String description = parts[1].trim();
        String values = parts.length > 2 ? parts[2].trim() : "";
        String defaultValue = parts.length > 3 ? parts[3].trim() : "";
        return new QuickFixConfigSetting(key, description, values, defaultValue);
    }

    /**
     * Represents a QuickFIX session configuration option.
     *
     * @param key the configuration key.
     * @param description the configuration description.
     * @param values the accepted values, when provided.
     * @param defaultValue the default value, when provided.
     */
    public record QuickFixConfigSetting(
            @NotNull String key,
            @NotNull String description,
            @NotNull String values,
            @NotNull String defaultValue
    ) {
    }
}
