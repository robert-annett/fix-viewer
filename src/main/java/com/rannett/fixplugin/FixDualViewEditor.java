package com.rannett.fixplugin;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.TextEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

public class FixDualViewEditor extends UserDataHolderBase implements FileEditor {
    private final JPanel panel;
    private final Editor editor;
    private FixTransposedTablePanel tablePanel;
    private boolean showingTable = false;
    private final VirtualFile file;
    private final Document document;

    public FixDualViewEditor(Project project, VirtualFile file) {
        this.file = file;
        panel = new JPanel(new BorderLayout());

        document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            throw new IllegalStateException("Could not load document for file: " + file.getName());
        }
        editor = EditorFactory.getInstance().createEditor(document, project);

        JButton toggleButton = new JButton("Toggle View");
        toggleButton.addActionListener(e -> toggleView());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.add(toggleButton);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(editor.getComponent(), BorderLayout.CENTER);

        // Listen to document changes
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                if (showingTable && tablePanel != null) {
                    List<String> messages = Arrays.asList(document.getText().split("\\R+"));
                    tablePanel.updateTable(messages);
                }
            }
        });
    }

    private void toggleView() {
        panel.remove(1);
        if (!showingTable) {
            List<String> messages = Arrays.asList(document.getText().split("\\R+"));
            tablePanel = new FixTransposedTablePanel(messages);
            panel.add(tablePanel, BorderLayout.CENTER);
        } else {
            panel.add(editor.getComponent(), BorderLayout.CENTER);
        }
        showingTable = !showingTable;
        panel.revalidate();
        panel.repaint();
    }

    @Override public @NotNull JComponent getComponent() { return panel; }
    @Override public @Nullable JComponent getPreferredFocusedComponent() { return showingTable ? tablePanel : editor.getContentComponent(); }
    @Override public @NotNull String getName() { return "FIX Dual View"; }
    @Override public void setState(@NotNull FileEditorState state) { }
    @Override public boolean isModified() { return false; }
    @Override public boolean isValid() { return true; }
    @Override public @NotNull FileEditorState getState(@NotNull FileEditorStateLevel level) { return new TextEditorState(); }
    @Override public @Nullable FileEditorLocation getCurrentLocation() { return null; }
    @Override public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) { }
    @Override public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) { }
    @Override public void dispose() { EditorFactory.getInstance().releaseEditor(editor); }
    @Override public @NotNull VirtualFile getFile() { return file; }
}
