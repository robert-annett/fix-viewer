package com.rannett.fixplugin;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
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
    private final FileEditor textEditor;  // Wrapped IntelliJ TextEditor
    private final JPanel mainPanel;
    private FixTransposedTablePanel tablePanel;
    private final Document document;
    private boolean showingTable = false;
    private final JScrollPane scrollPane;  // Holds the dynamic view
    private final VirtualFile file;

    public FixDualViewEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.file = file;
        this.textEditor = TextEditorProvider.getInstance().createEditor(project, file);
        this.document = ((TextEditor) textEditor).getEditor().getDocument();

        mainPanel = new JPanel(new BorderLayout());

        JButton toggleButton = new JButton("Toggle View");
        toggleButton.addActionListener(e -> toggleView());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.add(toggleButton);

        mainPanel.add(topBar, BorderLayout.NORTH);

        scrollPane = new JScrollPane(textEditor.getComponent());  // Start with text view
        mainPanel.add(scrollPane, BorderLayout.CENTER);

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
        scrollPane.setViewportView(null);

        if (!showingTable) {
            List<String> messages = Arrays.asList(document.getText().split("\\R+"));
            tablePanel = new FixTransposedTablePanel(messages);
            scrollPane.setViewportView(tablePanel);
        } else {
            scrollPane.setViewportView(textEditor.getComponent());
        }

        showingTable = !showingTable;
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    @Override public @NotNull JComponent getComponent() { return mainPanel; }
    @Override public @Nullable JComponent getPreferredFocusedComponent() {
        return showingTable ? tablePanel : textEditor.getPreferredFocusedComponent();
    }
    @Override public @NotNull String getName() { return "FIX Dual View"; }
    @Override public void setState(@NotNull FileEditorState state) { textEditor.setState(state); }
    @Override public boolean isModified() { return textEditor.isModified(); }
    @Override public boolean isValid() { return textEditor.isValid(); }
    @Override public @NotNull FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return textEditor.getState(level);
    }
    @Override public @Nullable FileEditorLocation getCurrentLocation() {
        return textEditor.getCurrentLocation();
    }
    @Override public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        textEditor.addPropertyChangeListener(listener);
    }
    @Override public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        textEditor.removePropertyChangeListener(listener);
    }
    @Override public void dispose() {
        textEditor.dispose();
    }
    @Override public @NotNull VirtualFile getFile() { return file; }
}
