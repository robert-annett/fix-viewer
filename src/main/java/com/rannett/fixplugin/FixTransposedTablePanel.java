package com.rannett.fixplugin;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FixTransposedTablePanel extends JPanel {
    private FixTransposedTableModel model;
    private JTable table;

    public FixTransposedTablePanel(List<String> fixMessages, FixTransposedTableModel.DocumentUpdater updater) {
        super(new BorderLayout());
        model = new FixTransposedTableModel(fixMessages, updater);
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void updateTable(List<String> fixMessages) {
        model.updateMessages(fixMessages);
    }

    public void highlightTagRow(String tag) {
        int row = model.getRowForTag(tag);
        if (row >= 0) {
            table.setRowSelectionInterval(row, row);
            table.scrollRectToVisible(table.getCellRect(row, 0, true));
        } else {
            table.clearSelection();
        }
    }

    public void clearHighlight() {
        table.clearSelection();
    }
}
