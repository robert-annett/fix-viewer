package com.rannett.fixplugin.ui.dictionary;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.io.InputStream;

/**
 * File editor providing the structured FIX dictionary viewer.
 */
public class FixDictionaryFileEditor extends UserDataHolderBase implements FileEditor {

    private static final Logger LOG = Logger.getInstance(FixDictionaryFileEditor.class);

    private final VirtualFile file;
    private final JPanel component;

    /**
     * Creates a new dictionary file editor for the provided file.
     *
     * @param project current project
     * @param file    dictionary file to render
     */
    public FixDictionaryFileEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.file = file;
        this.component = new JPanel(new BorderLayout());
        buildUi();
    }

    private void buildUi() {
        try (InputStream inputStream = file.getInputStream()) {
            FixDictionaryTreePanel panel = new FixDictionaryTreePanel(file.getPresentableUrl());
            panel.build(inputStream);
            component.removeAll();
            component.add(panel, BorderLayout.CENTER);
        } catch (Exception e) {
            LOG.warn("Failed to render dictionary viewer for " + file.getName(), e);
            component.removeAll();
            component.add(new javax.swing.JLabel("Unable to render dictionary: " + e.getMessage()), BorderLayout.CENTER);
        }
    }

    @Override
    /**
     * Provides the main UI component.
     *
     * @return editor component
     */
    public @NotNull JComponent getComponent() {
        return component;
    }

    @Override
    /**
     * Returns the preferred focus component.
     *
     * @return focus target
     */
    public @Nullable JComponent getPreferredFocusedComponent() {
        return component;
    }

    @Override
    /**
     * Identifies this editor type.
     *
     * @return editor name
     */
    public @NotNull String getName() {
        return "FIX Dictionary Viewer";
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void setState(@NotNull FileEditorState state) {
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public boolean isModified() {
        return false;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public boolean isValid() {
        return file.isValid();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void selectNotify() {
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void deselectNotify() {
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void dispose() {
    }
}
