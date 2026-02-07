package com.rannett.fixplugin.quickfix;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

/**
 * Language definition for QuickFIX session configuration files.
 */
public class QuickFixConfigLanguage extends Language {

    /**
     * Singleton instance for QuickFIX config language.
     */
    public static final QuickFixConfigLanguage INSTANCE = new QuickFixConfigLanguage();

    private QuickFixConfigLanguage() {
        super("QuickFixConfig");
    }

    /**
     * Returns the display name for the language.
     *
     * @return the language name.
     */
    @NotNull
    @Override
    public String getDisplayName() {
        return "QuickFIX Session Config";
    }
}
