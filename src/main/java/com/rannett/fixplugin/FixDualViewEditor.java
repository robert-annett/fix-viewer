package com.rannett.fixplugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.event.CaretListener;
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
    private final FixTransposedTablePanel tablePanel;
    private final Document document;
    private final VirtualFile file;
    private Integer pendingCaretOffset = null;

    public FixDualViewEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.file = file;
        this.textEditor = TextEditorProvider.getInstance().createEditor(project, file);
        this.document = ((TextEditor) textEditor).getEditor().getDocument();

        mainPanel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Text View", textEditor.getComponent());

        List<String> messages = Arrays.asList(document.getText().split("\\R+"));
        tablePanel = new FixTransposedTablePanel(messages, (msgId, tag, newValue) -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                String[] lines = document.getText().split("\\R+");
                int msgIndex = Integer.parseInt(msgId.replace("Message ", "")) - 1;
                if (msgIndex < 0 || msgIndex >= lines.length) return;
                String message = lines[msgIndex];
                int tagIndex = message.indexOf(tag + "=");
                if (tagIndex < 0) return;
                int valueStart = tagIndex + (tag + "=").length();
                int valueEnd = valueStart;
                while (valueEnd < message.length() && message.charAt(valueEnd) != '\u0001' && message.charAt(valueEnd) != '|')
                    valueEnd++;
                int lineStartOffset = document.getLineStartOffset(msgIndex);
                document.replaceString(lineStartOffset + valueStart, lineStartOffset + valueEnd, newValue);
            });
        }, project);
        tabbedPane.addTab("Transposed Table", tablePanel);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Full rebuild and revalidation on document change
        document.addDocumentListener(new com.intellij.openapi.editor.event.DocumentListener() {
            @Override
            public void documentChanged(@NotNull com.intellij.openapi.editor.event.DocumentEvent event) {
                SwingUtilities.invokeLater(() -> {
                    List<String> updatedMessages = Arrays.asList(document.getText().split("\\R+"));
                    tablePanel.updateTable(updatedMessages);
                });
            }
        });

        ((TextEditor) textEditor).getEditor().getCaretModel().addCaretListener(new CaretListener() {
            @Override
            public void caretPositionChanged(@NotNull com.intellij.openapi.editor.event.CaretEvent e) {
                Caret caret = e.getCaret();
                if (caret != null) {
                    String text = document.getText();
                    int offset = caret.getOffset();
                    Pattern pattern = Pattern.compile("(\\d+)=([^|\\u0001]*)");
                    Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        if (matcher.start() <= offset && matcher.end() >= offset) {
                            String tag = matcher.group(1);
                            String messageId = getMessageIdForOffset(text, offset, matcher.start());
                            tablePanel.highlightTagCell(tag, messageId);
                            return;
                        }
                    }
                    tablePanel.clearHighlight();
                }
            }

            private String getMessageIdForOffset(String text, int offset, int matchStart) {
                String[] lines = text.split("\\R+");
                int cumulative = 0;
                for (int i = 0; i < lines.length; i++) {
                    int lineLength = lines[i].length() + 1;
                    if (cumulative + lineLength > matchStart) {
                        return "Message " + (i + 1);
                    }
                    cumulative += lineLength;
                }
                return null;
            }
        });

        tablePanel.setOnCellSelected(() -> {
            String tag = tablePanel.getSelectedTag();
            String messageId = tablePanel.getSelectedMessageId();
            if (tag != null && messageId != null) {
                int offset = findTagOffsetInDocument(tag, messageId);
                if (offset >= 0) {
                    pendingCaretOffset = offset;
                }
            }
        });

        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            String selectedTitle = tabbedPane.getTitleAt(selectedIndex);
            if ("Text View".equals(selectedTitle) && pendingCaretOffset != null) {
                int offset = pendingCaretOffset;
                pendingCaretOffset = null;
                ApplicationManager.getApplication().invokeLater(() -> {
                    var editor = ((TextEditor) textEditor).getEditor();
                    editor.getCaretModel().moveToOffset(offset);
                    editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                    com.intellij.openapi.wm.IdeFocusManager.getInstance(project).requestFocus(editor.getContentComponent(), true);
                });
            }
        });
    }

    private int findTagOffsetInDocument(String tag, String messageId) {
        String[] lines = document.getText().split("\\R+");
        int msgIndex = Integer.parseInt(messageId.replace("Message ", "")) - 1;
        if (msgIndex < 0 || msgIndex >= lines.length) return -1;
        String message = lines[msgIndex];
        int tagIndex = message.indexOf(tag + "=");
        if (tagIndex < 0) return -1;
        int lineStartOffset = document.getLineStartOffset(msgIndex);
        return lineStartOffset + tagIndex;
    }

    @Override
    public @NotNull JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return tabbedPane.getSelectedIndex() == 0 ? textEditor.getPreferredFocusedComponent() : tablePanel;
    }

    @Override
    public @NotNull String getName() {
        return "FIX Dual View";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
        textEditor.setState(state);
    }

    @Override
    public boolean isModified() {
        return textEditor.isModified();
    }

    @Override
    public boolean isValid() {
        return textEditor.isValid();
    }

    @Override
    public @NotNull FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return textEditor.getState(level);
    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return textEditor.getCurrentLocation();
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        textEditor.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        textEditor.removePropertyChangeListener(listener);
    }

    @Override
    public void dispose() {
        textEditor.dispose();
    }

    @Override
    public @NotNull VirtualFile getFile() {
        return file;
    }
}
