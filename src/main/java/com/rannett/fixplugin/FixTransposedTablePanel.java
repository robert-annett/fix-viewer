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
}
