package com.rannett.fixplugin;

import com.intellij.ui.table.JBTable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

public class FixTransposedTablePanel extends JPanel {
    private final FixTransposedTableModel model;
    private final JBTable table;
    private Runnable onCellSelectedCallback;
    private String highlightedTag;
    private String highlightedMessageId;

    public FixTransposedTablePanel(List<String> fixMessages, FixTransposedTableModel.DocumentUpdater updater) {
        super(new BorderLayout());
        model = new FixTransposedTableModel(fixMessages, updater);
        table = new JBTable(model);
        table.setFillsViewportHeight(true);

        // Custom renderer to display value + description (but keep raw value for editing)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                String displayValue = String.valueOf(value);
                if (column >= 2) {  // Only apply to message columns
                    String tag = model.getTagAtRow(row);
                    String valStr = String.valueOf(value);
                    String desc = FixTagDictionary.getValueName(model.getFixVersion(), tag, valStr);
                    if (desc != null && !desc.isEmpty()) {
                        displayValue = valStr + " (" + desc + ")";
                    }
                }
                setText(displayValue);
                return c;
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
        if (row >= 0 && col >= 0) {
            table.changeSelection(row, col, false, false);
        } else if (row >= 0) {
            table.setRowSelectionInterval(row, row);
        } else {
            table.clearSelection();
        }
    }

    public void clearHighlight() {
        this.highlightedTag = null;
        this.highlightedMessageId = null;
        table.clearSelection();
    }
}
