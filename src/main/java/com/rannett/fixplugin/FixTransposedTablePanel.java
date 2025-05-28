package com.rannett.fixplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.table.JBTable;
import com.rannett.fixplugin.dictionary.FixDictionaryCache;
import com.rannett.fixplugin.dictionary.FixTagDictionary;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FixTransposedTablePanel extends JPanel {
    private FixTransposedTableModel model;
    private JBTable table;
    private Runnable onCellSelectedCallback;
    private String highlightedTag;
    private String highlightedMessageId;
    private final Project project;

    public FixTransposedTablePanel(List<String> fixMessages, FixTransposedTableModel.DocumentUpdater updater, Project project) {
        super(new BorderLayout());
        this.project = project;
        model = new FixTransposedTableModel(fixMessages, updater, project);
        table = new JBTable(model);
        table.setFillsViewportHeight(true);

        // Custom renderer to show value + description
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                String displayValue = String.valueOf(value);
                if (column >= 2) {
                    String tag = model.getTagAtRow(row);
                    String valStr = String.valueOf(value);
                    FixTagDictionary dictionary = FixDictionaryCache.getDictionary(project, model.getFixVersion());
                    String desc = dictionary.getValueName( tag, valStr);
                    if (desc != null && !desc.isEmpty()) {
                        displayValue = valStr + " (" + desc + ")";
                    }
                }
                setText(displayValue);
                return c;
            }
        });

        // Custom editor with ComboBox showing value (desc) but committing only value
        table.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()) {
            private ComboBox<String> comboBox;
            private Map<String, String> displayToValueMap;
            private String currentTag;

            @Override
            public Component getTableCellEditorComponent(JTable tbl, Object value, boolean isSelected, int row, int column) {
                currentTag = model.getTagAtRow(row);
                FixTagDictionary dictionary = FixDictionaryCache.getDictionary(project, model.getFixVersion());
                Map<String, String> enums = dictionary.getValueMap(currentTag);

                if (column >= 2 && enums != null && !enums.isEmpty()) {
                    displayToValueMap = new LinkedHashMap<>();
                    for (Map.Entry<String, String> entry : enums.entrySet()) {
                        String display = entry.getKey() + " (" + entry.getValue() + ")";
                        displayToValueMap.put(display, entry.getKey());
                    }
                    comboBox = new ComboBox<>(displayToValueMap.keySet().toArray(new String[0]));
                    comboBox.setEditable(true);

                    // Try to select the correct display value
                    String valStr = String.valueOf(value);
                    String currentDisplay = displayToValueMap.entrySet().stream()
                            .filter(e -> e.getValue().equals(valStr))
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse(valStr);  // Fallback to raw value
                    comboBox.setSelectedItem(currentDisplay);

                    // Commit on Enter
                    comboBox.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
                        @Override
                        public void keyPressed(java.awt.event.KeyEvent e) {
                            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                                stopCellEditing();
                            }
                        }
                    });
                    return comboBox;
                } else {
                    return super.getTableCellEditorComponent(tbl, value, isSelected, row, column);
                }
            }

            @Override
            public Object getCellEditorValue() {
                if (comboBox != null && comboBox.isDisplayable()) {
                    Object selected = comboBox.getEditor().getItem();
                    String rawValue = displayToValueMap != null && displayToValueMap.containsKey(selected)
                            ? displayToValueMap.get(selected)
                            : selected.toString();  // Allow custom values
                    comboBox = null;
                    displayToValueMap = null;
                    return rawValue;
                } else {
                    return super.getCellEditorValue();
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(e -> notifySelection());
        table.getColumnModel().getSelectionModel().addListSelectionListener(e -> notifySelection());
    }

    public void updateTable(List<String> fixMessages) {
        model.updateMessages(fixMessages);
    }

    public void setOnCellSelected(Runnable callback) {
        this.onCellSelectedCallback = callback;
    }

    private void notifySelection() {
        if (onCellSelectedCallback != null) onCellSelectedCallback.run();
    }

    public String getSelectedTag() {
        int row = table.getSelectedRow();
        return row >= 0 ? model.getTagAtRow(row) : null;
    }

    public String getSelectedMessageId() {
        int col = table.getSelectedColumn();
        return col >= 2 ? model.getMessageIdForColumn(col) : null;
    }

    public void highlightTagCell(String tag, String messageId) {
        this.highlightedTag = tag;
        this.highlightedMessageId = messageId;
        int row = model.getRowForTag(tag);
        int col = model.getColumnForMessageId(messageId);
        if (row >= 0 && col >= 0) table.changeSelection(row, col, false, false);
        else if (row >= 0) table.setRowSelectionInterval(row, row);
        else table.clearSelection();
    }

    public void clearHighlight() {
        this.highlightedTag = null;
        this.highlightedMessageId = null;
        table.clearSelection();
    }
}
