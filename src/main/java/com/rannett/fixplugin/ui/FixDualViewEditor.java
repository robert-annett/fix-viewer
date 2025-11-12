package com.rannett.fixplugin.ui;

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
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import com.rannett.fixplugin.util.FixMessageParser;
import com.rannett.fixplugin.dictionary.FixDictionaryChangeListener;

public class FixDualViewEditor extends UserDataHolderBase implements FileEditor {
    private final FileEditor textEditor;
    private final JPanel mainPanel;
    private final JTabbedPane tabbedPane;
    private final FixTransposedTablePanel tablePanel;
    private final FixMessageTreePanel treePanel;
    private final FixCommTimelinePanel commPanel;
    private final Document document;
    private final VirtualFile file;
    private final MessageBusConnection messageBusConnection;
    private Integer pendingCaretOffset = null;

    public FixDualViewEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.file = file;
        this.textEditor = TextEditorProvider.getInstance().createEditor(project, file);
        this.document = ((TextEditor) textEditor).getEditor().getDocument();

        mainPanel = new JPanel(new BorderLayout());
        tabbedPane = new JBTabbedPane();

        tabbedPane.addTab("Text View", textEditor.getComponent());

        List<String> messages = FixMessageParser.splitMessages(document.getText());
        tablePanel = new FixTransposedTablePanel(messages, (msgId, tag, occurrence, newValue) -> WriteCommandAction.runWriteCommandAction(project, () -> {
            String[] lines = document.getText().split("\\R+");
            int msgIndex = Integer.parseInt(msgId.replace("Message ", "")) - 1;
            if (msgIndex < 0 || msgIndex >= lines.length) return;
            String message = lines[msgIndex];
            int tagIndex = -1;
            int fromIndex = 0;
            for (int occ = 1; occ <= occurrence; occ++) {
                tagIndex = message.indexOf(tag + "=", fromIndex);
                if (tagIndex < 0) break;
                fromIndex = tagIndex + 1;
            }
            if (tagIndex < 0) return;
            int valueStart = tagIndex + (tag + "=").length();
            int valueEnd = valueStart;
            while (valueEnd < message.length() && message.charAt(valueEnd) != '\u0001' && message.charAt(valueEnd) != '|')
                valueEnd++;
            int lineStartOffset = document.getLineStartOffset(msgIndex);
            document.replaceString(lineStartOffset + valueStart, lineStartOffset + valueEnd, newValue);
        }), project);
        tabbedPane.addTab("Transposed Table", tablePanel);

        treePanel = new FixMessageTreePanel(messages, project);
        tabbedPane.addTab("Tree View", treePanel);

        commPanel = new FixCommTimelinePanel(messages);
        commPanel.setOnMessageSelected(idx -> {
            int offset = findMessageOffset(idx);
            if (offset >= 0) {
                pendingCaretOffset = offset;
                tablePanel.highlightTagCell("8", "Message " + idx);
            }
        });
        tabbedPane.addTab("Message Flow", commPanel);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        messageBusConnection = project.getMessageBus().connect(this);
        messageBusConnection.subscribe(FixDictionaryChangeListener.TOPIC, this::handleDictionaryChange);

        // Full rebuild and revalidation on document change
        document.addDocumentListener(new com.intellij.openapi.editor.event.DocumentListener() {
            @Override
            public void documentChanged(@NotNull com.intellij.openapi.editor.event.DocumentEvent event) {
                SwingUtilities.invokeLater(() -> {
                    List<String> updatedMessages = FixMessageParser.splitMessages(document.getText());
                    tablePanel.updateTable(updatedMessages);
                    treePanel.updateTree(updatedMessages);
                    commPanel.updateMessages(updatedMessages);
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
                            String messageId = getMessageIdForOffset(text, matcher.start());
                            tablePanel.highlightTagCell(tag, messageId);
                            return;
                        }
                    }
                    tablePanel.clearHighlight();
                }
            }

            private String getMessageIdForOffset(String text, int matchStart) {
                List<String> lines = FixMessageParser.splitMessages(text);
                int cumulative = 0;
                for (int i = 0; i < lines.size(); i++) {
                    int lineLength = lines.get(i).length() + 1;
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

    private void handleDictionaryChange() {
        ApplicationManager.getApplication().invokeLater(() -> {
            List<String> updatedMessages = ApplicationManager.getApplication().runReadAction(
                    (Computable<List<String>>) () -> FixMessageParser.splitMessages(document.getText())
            );
            tablePanel.refreshDictionaryMetadata();
            treePanel.updateTree(updatedMessages);
        });
    }

    private int findTagOffsetInDocument(String tag, String messageId) {
        List<String> lines = FixMessageParser.splitMessages(document.getText());
        int msgIndex = Integer.parseInt(messageId.replace("Message ", "")) - 1;
        if (msgIndex < 0 || msgIndex >= lines.size()) return -1;
        String message = lines.get(msgIndex);
        int tagIndex = message.indexOf(tag + "=");
        if (tagIndex < 0) return -1;
        int offset = 0;
        for (int i = 0; i < msgIndex; i++) {
            offset += lines.get(i).length() + 1;
        }
        return offset + tagIndex;
    }

    private int findMessageOffset(int messageIndex) {
        List<String> lines = FixMessageParser.splitMessages(document.getText());
        if (messageIndex < 1 || messageIndex > lines.size()) {
            return -1;
        }
        return IntStream.range(0, messageIndex - 1)
                .map(i -> lines.get(i).length() + 1)
                .sum();
    }

    @Override
    public @NotNull JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        int index = tabbedPane.getSelectedIndex();
        if (index == 0) {
            return textEditor.getPreferredFocusedComponent();
        }
        if (index == 1) {
            return tablePanel;
        }
        if (index == 2) {
            return treePanel;
        }
        return commPanel;
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
        messageBusConnection.disconnect();
        textEditor.dispose();
    }

    @Override
    public @NotNull VirtualFile getFile() {
        return file;
    }
}
