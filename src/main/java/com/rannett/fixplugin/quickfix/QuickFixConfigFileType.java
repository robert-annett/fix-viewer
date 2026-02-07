package com.rannett.fixplugin.quickfix;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.rannett.fixplugin.FixIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * File type for QuickFIX session configuration files.
 */
public class QuickFixConfigFileType extends LanguageFileType {

    /**
     * Singleton instance for the QuickFIX config file type.
     */
    public static final QuickFixConfigFileType INSTANCE = new QuickFixConfigFileType();

    private QuickFixConfigFileType() {
        super(QuickFixConfigLanguage.INSTANCE);
    }

    /**
     * Returns the name of the file type.
     *
     * @return the file type name.
     */
    @NotNull
    @Override
    public String getName() {
        return "QuickFIX Session Config";
    }

    /**
     * Returns the description of the file type.
     *
     * @return the file type description.
     */
    @NotNull
    @Override
    public String getDescription() {
        return "QuickFIX session configuration";
    }

    /**
     * Returns the default extension for the file type.
     *
     * @return the default extension.
     */
    @NotNull
    @Override
    public String getDefaultExtension() {
        return "fix.cfg";
    }

    /**
     * Returns the icon for the file type.
     *
     * @return the file type icon.
     */
    @Nullable
    @Override
    public Icon getIcon() {
        return FixIcons.FILE;
    }
}
