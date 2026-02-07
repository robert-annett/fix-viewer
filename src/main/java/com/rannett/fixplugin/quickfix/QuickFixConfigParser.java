package com.rannett.fixplugin.quickfix;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Parser for QuickFIX session configuration files.
 */
public class QuickFixConfigParser implements PsiParser {

    /**
     * Parses the QuickFIX session configuration file into PSI.
     *
     * @param root the root element type.
     * @param builder the PSI builder.
     * @return the root AST node.
     */
    @Override
    public @NotNull ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
        PsiBuilder.Marker rootMarker = builder.mark();
        while (!builder.eof()) {
            builder.advanceLexer();
        }
        rootMarker.done(root);
        return builder.getTreeBuilt();
    }
}
