package com.rannett.fixplugin.quickfix;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Parser definition for QuickFIX session configuration files.
 */
public class QuickFixConfigParserDefinition implements ParserDefinition {

    /**
     * File element type for QuickFIX session configuration files.
     */
    public static final IFileElementType FILE = new IFileElementType(QuickFixConfigLanguage.INSTANCE);

    /**
     * Creates the lexer for QuickFIX session configuration files.
     *
     * @param project the project context.
     * @return the lexer instance.
     */
    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new QuickFixConfigLexer();
    }

    /**
     * Returns the comment token set for QuickFIX session configuration files.
     *
     * @return the comment token set.
     */
    @Override
    public @NotNull TokenSet getCommentTokens() {
        return TokenSet.create(QuickFixConfigTokenType.COMMENT);
    }

    /**
     * Returns the string literal token set for QuickFIX session configuration files.
     *
     * @return the string literal token set.
     */
    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    /**
     * Creates the parser for QuickFIX session configuration files.
     *
     * @param project the project context.
     * @return the parser instance.
     */
    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new QuickFixConfigParser();
    }

    /**
     * Returns the file node type for QuickFIX session configuration files.
     *
     * @return the file node type.
     */
    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    /**
     * Creates the PSI file for QuickFIX session configuration files.
     *
     * @param viewProvider the file view provider.
     * @return the PSI file.
     */
    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new QuickFixConfigFile(viewProvider);
    }

    /**
     * Creates PSI elements for QuickFIX session configuration tokens.
     *
     * @param node the AST node.
     * @return the PSI element.
     */
    @Override
    public @NotNull PsiElement createElement(@NotNull ASTNode node) {
        return new ASTWrapperPsiElement(node);
    }
}
