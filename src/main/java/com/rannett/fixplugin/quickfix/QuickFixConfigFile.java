package com.rannett.fixplugin.quickfix;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

/**
 * PSI file wrapper for QuickFIX session configuration files.
 */
public class QuickFixConfigFile extends PsiFileBase {

    /**
     * Creates a PSI file wrapper for QuickFIX session configuration files.
     *
     * @param viewProvider the view provider.
     */
    public QuickFixConfigFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, QuickFixConfigLanguage.INSTANCE);
    }

    /**
     * Returns the file type for QuickFIX session configuration files.
     *
     * @return the file type.
     */
    @Override
    public @NotNull FileType getFileType() {
        return QuickFixConfigFileType.INSTANCE;
    }

    /**
     * Returns a presentable name for the PSI file.
     *
     * @return the presentable name.
     */
    @Override
    public @NotNull String toString() {
        return "QuickFIX Session Config File";
    }
}
