package com.rannett.fixplugin.inspection;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.rannett.fixplugin.quickfix.QuickFixConfigLanguage;
import com.rannett.fixplugin.quickfix.QuickFixConfigSettings;
import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Inspection that validates QuickFIX session configuration values.
 */
public class QuickFixConfigValueInspection extends LocalInspectionTool {

    private static final Pattern LINE_PATTERN = Pattern.compile("(?m)^.*$");

    /**
     * {@inheritDoc}
     */
    @Override
    public ProblemDescriptor @Nullable [] checkFile(
            @NotNull PsiFile file,
            @NotNull InspectionManager manager,
            boolean isOnTheFly
    ) {
        if (!file.getLanguage().isKindOf(QuickFixConfigLanguage.INSTANCE)) {
            return ProblemDescriptor.EMPTY_ARRAY;
        }

        String text = file.getText();
        if (text == null || text.isBlank()) {
            return ProblemDescriptor.EMPTY_ARRAY;
        }

        List<ProblemDescriptor> problems = LINE_PATTERN.matcher(text).results()
                .map(result -> validateLine(result, text, file, manager, isOnTheFly))
                .flatMap(Optional::stream)
                .toList();

        if (problems.isEmpty()) {
            return ProblemDescriptor.EMPTY_ARRAY;
        }

        return problems.toArray(ProblemDescriptor[]::new);
    }

    private Optional<ProblemDescriptor> validateLine(
            MatchResult match,
            String text,
            PsiFile file,
            InspectionManager manager,
            boolean isOnTheFly
    ) {
        int lineStart = match.start();
        int lineEnd = match.end();
        if (lineStart >= lineEnd) {
            return Optional.empty();
        }

        String line = text.substring(lineStart, lineEnd);
        String trimmedLine = line.trim();
        if (trimmedLine.isEmpty()) {
            return Optional.empty();
        }
        if (trimmedLine.startsWith("[") || isComment(trimmedLine)) {
            return Optional.empty();
        }

        int equalsIndex = line.indexOf('=');
        if (equalsIndex < 0) {
            return Optional.empty();
        }

        String key = line.substring(0, equalsIndex).trim();
        if (key.isEmpty()) {
            return Optional.empty();
        }

        ValueRange valueRange = extractValueRange(line, equalsIndex);
        if (valueRange.start() < 0) {
            return Optional.empty();
        }

        String value = line.substring(valueRange.start(), valueRange.end());
        Optional<String> error = QuickFixConfigSettings.validateValue(key, value);
        if (error.isEmpty()) {
            return Optional.empty();
        }

        TextRange range = TextRange.create(lineStart + valueRange.start(), lineStart + valueRange.end());
        ProblemDescriptor descriptor = manager.createProblemDescriptor(
                file,
                range,
                error.get(),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly
        );
        return Optional.of(descriptor);
    }

    private boolean isComment(String trimmedLine) {
        if (trimmedLine.startsWith("#") || trimmedLine.startsWith(";")) {
            return true;
        }
        return trimmedLine.startsWith("//");
    }

    private ValueRange extractValueRange(String line, int equalsIndex) {
        int start = equalsIndex + 1;
        while (start < line.length() && Character.isWhitespace(line.charAt(start))) {
            start++;
        }
        int end = line.length();
        while (end > start && Character.isWhitespace(line.charAt(end - 1))) {
            end--;
        }
        if (start >= end) {
            return new ValueRange(-1, -1);
        }
        return new ValueRange(start, end);
    }

    private record ValueRange(int start, int end) {
    }
}
