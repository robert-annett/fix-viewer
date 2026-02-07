package com.rannett.fixplugin.quickfix;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Token types used for QuickFIX session configuration highlighting.
 */
public class QuickFixConfigTokenType extends IElementType {

    /**
     * Section header token.
     */
    public static final QuickFixConfigTokenType SECTION = new QuickFixConfigTokenType("SECTION");
    /**
     * Key token for configuration entries.
     */
    public static final QuickFixConfigTokenType KEY = new QuickFixConfigTokenType("KEY");
    /**
     * Separator token for '='.
     */
    public static final QuickFixConfigTokenType SEPARATOR = new QuickFixConfigTokenType("SEPARATOR");
    /**
     * Value token for configuration entries.
     */
    public static final QuickFixConfigTokenType VALUE = new QuickFixConfigTokenType("VALUE");
    /**
     * Comment token for configuration entries.
     */
    public static final QuickFixConfigTokenType COMMENT = new QuickFixConfigTokenType("COMMENT");

    private QuickFixConfigTokenType(@NotNull String debugName) {
        super(debugName, QuickFixConfigLanguage.INSTANCE);
    }
}
