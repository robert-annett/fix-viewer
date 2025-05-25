package com.rannett.fixplugin;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
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
    private final JPanel tablePanel;
    private boolean showingTable = false;
    private final VirtualFile file;

    public FixDualViewEditor(Project project, VirtualFile file) {
        this.file = file;
        panel = new JPanel(new BorderLayout());

        // Obtain document and editor
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            throw new IllegalStateException("Could not load document for file: " + file.getName());
        }
        editor = EditorFactory.getInstance().createEditor(document, project);

        // Top bar with toggle button
        JButton toggleButton = new JButton("Toggle View");
        toggleButton.addActionListener(e -> toggleView());
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.add(toggleButton);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(editor.getComponent(), BorderLayout.CENTER);

        // Prepare transposed table view
        List<String> messages = Arrays.asList(document.getText().split("\\R+"));
        tablePanel = new FixTransposedTablePanel(messages);
    }

    private void toggleView() {
        panel.remove(1); // remove current center component
        panel.add(showingTable ? editor.getComponent() : tablePanel, BorderLayout.CENTER);
        showingTable = !showingTable;
        panel.revalidate();
        panel.repaint();
    }

    @Override
    public @NotNull JComponent getComponent() {
        return panel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return showingTable ? tablePanel : editor.getContentComponent();
    }

    @Override
    public @NotNull String getName() {
        return "FIX Dual View";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
        // No-op for now; you can sync state if needed later
    }

    @Override
    public boolean isModified() {
        return false; // Not editable for now
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public @NotNull FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return new TextEditorState(); // Simple state; customize if needed
    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        // Caret tracking can be implemented later; for now, return null or a basic location
        return null;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        // No-op; implement if you need to notify listeners of property changes
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        // No-op
    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(editor);
    }

    @Override
    public @NotNull VirtualFile getFile() {
        return file;
    }
}
