package com.rannett.fixplugin.quickfix;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Syntax highlighter for QuickFIX session configuration files.
 */
public class QuickFixConfigSyntaxHighlighter implements SyntaxHighlighter {

    /**
     * Highlighting for section headers.
     */
    public static final TextAttributesKey SECTION = TextAttributesKey.createTextAttributesKey(
            "QUICKFIX_CONFIG_SECTION",
            DefaultLanguageHighlighterColors.METADATA
    );
    /**
     * Highlighting for keys.
     */
    public static final TextAttributesKey KEY = TextAttributesKey.createTextAttributesKey(
            "QUICKFIX_CONFIG_KEY",
            DefaultLanguageHighlighterColors.KEYWORD
    );
    /**
     * Highlighting for separators.
     */
    public static final TextAttributesKey SEPARATOR = TextAttributesKey.createTextAttributesKey(
            "QUICKFIX_CONFIG_SEPARATOR",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
    );
    /**
     * Highlighting for values.
     */
    public static final TextAttributesKey VALUE = TextAttributesKey.createTextAttributesKey(
            "QUICKFIX_CONFIG_VALUE",
            DefaultLanguageHighlighterColors.STRING
    );
    /**
     * Highlighting for comments.
     */
    public static final TextAttributesKey COMMENT = TextAttributesKey.createTextAttributesKey(
            "QUICKFIX_CONFIG_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
    );

    private static final TextAttributesKey[] SECTION_KEYS = new TextAttributesKey[]{SECTION};
    private static final TextAttributesKey[] KEY_KEYS = new TextAttributesKey[]{KEY};
    private static final TextAttributesKey[] SEPARATOR_KEYS = new TextAttributesKey[]{SEPARATOR};
    private static final TextAttributesKey[] VALUE_KEYS = new TextAttributesKey[]{VALUE};
    private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    /**
     * Returns the lexer for this highlighter.
     *
     * @return the lexer.
     */
    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new QuickFixConfigLexer();
    }

    /**
     * Returns the highlighting attributes for the token.
     *
     * @param tokenType the token type.
     * @return the text attributes for the token.
     */
    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (tokenType == QuickFixConfigTokenType.SECTION) {
            return SECTION_KEYS;
        }
        if (tokenType == QuickFixConfigTokenType.KEY) {
            return KEY_KEYS;
        }
        if (tokenType == QuickFixConfigTokenType.SEPARATOR) {
            return SEPARATOR_KEYS;
        }
        if (tokenType == QuickFixConfigTokenType.VALUE) {
            return VALUE_KEYS;
        }
        if (tokenType == QuickFixConfigTokenType.COMMENT || tokenType == TokenType.BAD_CHARACTER) {
            return COMMENT_KEYS;
        }
        return EMPTY_KEYS;
    }
}
