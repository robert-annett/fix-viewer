package com.rannett.fixplugin.quickfix;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for QuickFIX session configuration syntax highlighters.
 */
public class QuickFixConfigSyntaxHighlighterFactory extends SyntaxHighlighterFactory {

    /**
     * Returns a syntax highlighter instance.
     *
     * @param project the current project.
     * @param virtualFile the file to highlight.
     * @return the syntax highlighter.
     */
    @NotNull
    @Override
    public SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile) {
        return new QuickFixConfigSyntaxHighlighter();
    }
}
