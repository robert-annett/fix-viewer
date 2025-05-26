package com.rannett.fixplugin;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FixDualViewEditor extends UserDataHolderBase implements FileEditor {
    private final FileEditor textEditor;
    private final JPanel mainPanel;
    private final JTabbedPane tabbedPane;
    private FixTransposedTablePanel tablePanel;
    private final Document document;
    private final VirtualFile file;
    private String caretTag = null;

    public FixDualViewEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.file = file;
        this.textEditor = TextEditorProvider.getInstance().createEditor(project, file);
        this.document = ((TextEditor) textEditor).getEditor().getDocument();

        mainPanel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Text View", textEditor.getComponent());

        List<String> messages = Arrays.asList(document.getText().split("\\R+"));
        tablePanel = new FixTransposedTablePanel(messages, new FixTransposedTableModel.DocumentUpdater() {
            @Override
            public void updateTagValueInMessage(String messageId, String tag, String newValue) {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    String[] lines = document.getText().split("\\R+");
                    int msgIndex = Integer.parseInt(messageId.replace("Message ", "")) - 1;
                    if (msgIndex < 0 || msgIndex >= lines.length) return;

                    String message = lines[msgIndex];
                    int tagIndex = message.indexOf(tag + "=");
                    if (tagIndex < 0) return;

                    int valueStart = tagIndex + (tag + "=").length();
                    int valueEnd = valueStart;
                    while (valueEnd < message.length() && message.charAt(valueEnd) != '\u0001' && message.charAt(valueEnd) != '|') {
                        valueEnd++;
                    }

                    int lineStartOffset = getLineStartOffset(document, msgIndex);
                    int startOffset = lineStartOffset + valueStart;
                    int endOffset = lineStartOffset + valueEnd;

                    document.replaceString(startOffset, endOffset, newValue);
                });
            }

            private int getLineStartOffset(Document doc, int lineNumber) {
                return doc.getLineStartOffset(lineNumber);
            }
        });
        tabbedPane.addTab("Transposed Table", tablePanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Live update table when document changes
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                List<String> updatedMessages = Arrays.asList(document.getText().split("\\R+"));
                tablePanel.updateTable(updatedMessages);
            }
        });

        // Caret tracking
        ((TextEditor) textEditor).getEditor().getCaretModel().addCaretListener(new CaretListener() {
            @Override
            public void caretPositionChanged(@NotNull CaretEvent e) {
                Caret caret = e.getCaret();
                if (caret != null) {
                    String text = document.getText();
                    int offset = caret.getOffset();
                    Pattern pattern = Pattern.compile("(\\d+)=([^|\\u0001]*)");
                    Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        if (matcher.start() <= offset && matcher.end() >= offset) {
                            caretTag = matcher.group(1);
                            tablePanel.highlightTagRow(caretTag);
                            return;
                        }
                    }
                    caretTag = null;
                    tablePanel.clearHighlight();
                }
            }
        });
    }

    @Override public @NotNull JComponent getComponent() { return mainPanel; }
    @Override public @Nullable JComponent getPreferredFocusedComponent() {
        return tabbedPane.getSelectedIndex() == 0 ? textEditor.getPreferredFocusedComponent() : tablePanel;
    }
    @Override public @NotNull String getName() { return "FIX Dual View"; }
    @Override public void setState(@NotNull FileEditorState state) { textEditor.setState(state); }
    @Override public boolean isModified() { return textEditor.isModified(); }
    @Override public boolean isValid() { return textEditor.isValid(); }
    @Override public @NotNull FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return textEditor.getState(level);
    }
    @Override public @Nullable FileEditorLocation getCurrentLocation() { return textEditor.getCurrentLocation(); }
    @Override public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        textEditor.addPropertyChangeListener(listener);
    }
    @Override public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        textEditor.removePropertyChangeListener(listener);
    }
    @Override public void dispose() { textEditor.dispose(); }
    @Override public @NotNull VirtualFile getFile() { return file; }
}
