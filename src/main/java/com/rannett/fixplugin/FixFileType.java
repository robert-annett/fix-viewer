package com.rannett.fixplugin;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FixFileType extends LanguageFileType {

    public static final FixFileType INSTANCE = new FixFileType();

    private FixFileType() {
        super(FixLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Fix File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Fix language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "fix";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return FixIcons.FILE;
    }

}