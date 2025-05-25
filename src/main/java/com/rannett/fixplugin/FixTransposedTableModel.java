package com.rannett.fixplugin;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.stream.Collectors;

public class FixTransposedTableModel extends AbstractTableModel {
    private List<String> columnHeaders;
    private List<String> tagOrder;
    private Map<String, Map<String, String>> transposed;
    private String fixVersion;
    private final DocumentUpdater documentUpdater;

    public FixTransposedTableModel(List<String> fixMessages, DocumentUpdater updater) {
        this.documentUpdater = updater;
        buildModel(fixMessages);
    }

    public void updateMessages(List<String> fixMessages) {
        int oldRowCount = getRowCount();
        int oldColCount = getColumnCount();

        buildModel(fixMessages);

        SwingUtilities.invokeLater(() -> {
            if (getRowCount() != oldRowCount || getColumnCount() != oldColCount) {
                fireTableStructureChanged();
            } else {
                fireTableDataChanged();
            }
        });
    }

    private void buildModel(List<String> fixMessages) {
        this.columnHeaders = new ArrayList<>();
        this.transposed = new LinkedHashMap<>();

        String version = detectFixVersion(fixMessages.isEmpty() ? "" : fixMessages.get(0));
        this.fixVersion = version != null ? version : "FIX.4.2";

        Set<String> allTags = new LinkedHashSet<>();
        List<String> firstMessageTags = new ArrayList<>();
        int i = 1;

        for (String message : fixMessages) {
            message = message.trim();
            if (message.isEmpty() || message.startsWith("#")) continue;  // Skip comments and blank lines

            String msgId = "Message " + i++;
            columnHeaders.add(msgId);
            Map<String, String> tagValueMap = parseFixMessage(message);

            if (i == 2) {
                firstMessageTags.addAll(tagValueMap.keySet());
            }

            for (String tag : tagValueMap.keySet()) {
                allTags.add(tag);
                transposed.computeIfAbsent(tag, k -> new LinkedHashMap<>()).put(msgId, tagValueMap.get(tag));
            }
        }

        this.tagOrder = new ArrayList<>(firstMessageTags);
        for (String tag : allTags) {
            if (!this.tagOrder.contains(tag)) {
                this.tagOrder.add(tag);
            }
        }
    }

    private String detectFixVersion(String message) {
        return Arrays.stream(message.split("[|\\u0001]"))
                .filter(s -> s.startsWith("8="))
                .map(s -> s.substring(2))
                .findFirst()
                .orElse(null);
    }

    private Map<String, String> parseFixMessage(String msg) {
        return Arrays.stream(msg.split("[|\\u0001]"))
                .map(p -> p.split("=", 2))
                .filter(p -> p.length == 2)
                .collect(Collectors.toMap(p -> p[0], p -> p[1], (a, b) -> b, LinkedHashMap::new));
    }

    @Override public int getRowCount() { return tagOrder.size(); }

    @Override public int getColumnCount() { return 2 + columnHeaders.size(); }

    @Override public String getColumnName(int column) {
        if (column == 0) return "Tag";
        if (column == 1) return "Name";
        return columnHeaders.get(column - 2);
    }

    @Override public Object getValueAt(int rowIndex, int columnIndex) {
        String tag = tagOrder.get(rowIndex);
        if (columnIndex == 0) return tag;
        if (columnIndex == 1) return FixTagDictionary.getTagName(fixVersion, tag);
        String msgId = columnHeaders.get(columnIndex - 2);
        return transposed.getOrDefault(tag, Collections.emptyMap()).getOrDefault(msgId, "");
    }

    @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex >= 2;  // Only message columns are editable
    }

    @Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String tag = tagOrder.get(rowIndex);
        String msgId = columnHeaders.get(columnIndex - 2);
        String newValue = aValue.toString();

        transposed.get(tag).put(msgId, newValue);
        documentUpdater.updateTagValueInMessage(msgId, tag, newValue);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public interface DocumentUpdater {
        void updateTagValueInMessage(String messageId, String tag, String newValue);
    }
}
