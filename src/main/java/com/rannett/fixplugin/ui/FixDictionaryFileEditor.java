package com.rannett.fixplugin.ui;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class FixDictionaryFileEditor extends UserDataHolderBase implements FileEditor {
    private final TextEditor delegate;
    private final VirtualFile file;

    public FixDictionaryFileEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.file = file;
        this.delegate = (TextEditor) TextEditorProvider.getInstance().createEditor(project, file);
    }

    @Override
    public @NotNull JComponent getComponent() {
        return delegate.getComponent();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return delegate.getPreferredFocusedComponent();
    }

    @Override
    public @NotNull String getName() {
        return "FIX Dictionary";
    }

    @Override
    public @NotNull VirtualFile getFile() {
        return file;
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
        delegate.setState(state);
    }

    @Override
    public boolean isModified() {
        return delegate.isModified();
    }

    @Override
    public boolean isValid() {
        return delegate.isValid();
    }

    @Override
    public void selectNotify() {
        delegate.selectNotify();
    }

    @Override
    public void deselectNotify() {
        delegate.deselectNotify();
    }

    @Override
    public void addPropertyChangeListener(@NotNull java.beans.PropertyChangeListener listener) {
        delegate.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NotNull java.beans.PropertyChangeListener listener) {
        delegate.removePropertyChangeListener(listener);
    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return delegate.getCurrentLocation();
    }

    @Override
    public @NotNull FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return delegate.getState(level);
    }

    @Override
    public void dispose() {
        delegate.dispose();
    }
}
