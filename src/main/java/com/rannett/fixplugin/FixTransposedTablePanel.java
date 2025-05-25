package com.rannett.fixplugin;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FixTransposedTablePanel extends JPanel {
    public FixTransposedTablePanel(List<String> fixMessages) {
        super(new BorderLayout());
        FixTransposedTableModel model = new FixTransposedTableModel(fixMessages);
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}
