package com.rannett.fixplugin.ui;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FixDictionaryFileEditor extends UserDataHolderBase implements FileEditor {
    private static final int SOURCE_TAB_INDEX = 0;
    private static final int MESSAGE_TREE_TAB_INDEX = 1;

    private final TextEditor delegate;
    private final VirtualFile file;
    private final JPanel panel;
    private final FixDictionaryTreePanel treePanel;
    private final Document document;
    private final DocumentListener documentListener;
    private final ChangeListener tabSelectionListener;
    private final JBTabbedPane tabbedPane;
    private boolean dictionaryTreeDirty;
    private int dictionaryTreeRefreshGeneration;

    public FixDictionaryFileEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.file = file;
        this.delegate = (TextEditor) TextEditorProvider.getInstance().createEditor(project, file);
        this.document = delegate.getEditor().getDocument();
        this.treePanel = new FixDictionaryTreePanel(document.getText(), this::navigateToFieldDefinition);
        tabbedPane = new JBTabbedPane();
        tabbedPane.addTab("Source", delegate.getComponent());
        tabbedPane.addTab("Message Tree", treePanel);
        this.tabSelectionListener = event -> scheduleDictionaryTreeRefresh(project);
        tabbedPane.addChangeListener(tabSelectionListener);
        this.documentListener = new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                dictionaryTreeDirty = true;
                scheduleDictionaryTreeRefresh(project);
            }
        };
        document.addDocumentListener(documentListener);

        this.panel = new JPanel(new BorderLayout());
        this.panel.add(tabbedPane, BorderLayout.CENTER);
    }

    @Override
    public @NotNull JComponent getComponent() {
        return panel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return delegate.getPreferredFocusedComponent();
    }

    @Override
    public @NotNull String getName() {
        return "FIX Dictionary";
    }

    private void scheduleDictionaryTreeRefresh(@NotNull Project project) {
        if (tabbedPane.getSelectedIndex() != MESSAGE_TREE_TAB_INDEX) {
            return;
        }

        int refreshGeneration = ++dictionaryTreeRefreshGeneration;
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()
                    || refreshGeneration != dictionaryTreeRefreshGeneration
                    || !dictionaryTreeDirty
                    || tabbedPane.getSelectedIndex() != MESSAGE_TREE_TAB_INDEX) {
                return;
            }
            dictionaryTreeDirty = false;
            treePanel.updateDictionaryText(document.getText());
        });
    }

    private void navigateToFieldDefinition(String fieldName) {
        int offset = findFieldDefinitionOffset(fieldName);
        if (offset < 0) {
            return;
        }
        tabbedPane.setSelectedIndex(SOURCE_TAB_INDEX);
        delegate.getEditor().getCaretModel().moveToOffset(offset);
        delegate.getEditor().getScrollingModel().scrollToCaret(ScrollType.CENTER);
        delegate.getEditor().getSelectionModel().removeSelection();
    }

    private int findFieldDefinitionOffset(String fieldName) {
        String text = document.getText();
        Pattern fieldsSectionPattern = Pattern.compile("(?is)<\\s*fields\\b.*?</\\s*fields\\s*>");
        Matcher fieldsSectionMatcher = fieldsSectionPattern.matcher(text);
        if (fieldsSectionMatcher.find()) {
            int offset = findFieldNameOffset(fieldsSectionMatcher.group(), fieldName);
            if (offset >= 0) {
                return fieldsSectionMatcher.start() + offset;
            }
        }
        return findFieldNameOffset(text, fieldName);
    }

    private static int findFieldNameOffset(String text, String fieldName) {
        Pattern fieldPattern = Pattern.compile("(?is)<\\s*field\\b[^>]*\\bname\\s*=\\s*([\"'])"
                + Pattern.quote(fieldName)
                + "\\1");
        Matcher fieldMatcher = fieldPattern.matcher(text);
        if (!fieldMatcher.find()) {
            return -1;
        }
        return fieldMatcher.start(1) + 1;
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
        document.removeDocumentListener(documentListener);
        tabbedPane.removeChangeListener(tabSelectionListener);
        delegate.dispose();
    }
}
