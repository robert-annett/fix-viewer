package com.rannett.fixplugin.ui;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.rannett.fixplugin.dictionary.FixDictionaryXmlUtil;
import org.jetbrains.annotations.NotNull;

public class FixDictionaryFileEditorProvider implements FileEditorProvider, DumbAware {
    private static final Logger LOG = Logger.getInstance(FixDictionaryFileEditorProvider.class);

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        boolean accepted = FixDictionaryXmlUtil.isFixDictionaryFile(file);
        LOG.warn("FIX dictionary editor accept(" + file.getPath() + ") = " + accepted);
        return accepted;
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        LOG.warn("Creating FIX dictionary editor for: " + file.getPath());
        return new FixDictionaryFileEditor(project, file);
    }

    @Override
    public @NotNull String getEditorTypeId() {
        return "fix-dictionary-view";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }
}
