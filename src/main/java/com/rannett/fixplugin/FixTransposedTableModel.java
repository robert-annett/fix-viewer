package com.rannett.fixplugin;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.stream.Collectors;

public class FixTransposedTableModel extends AbstractTableModel {
    private final List<String> columnHeaders;
    private final List<String> tagOrder;
    private final Map<String, Map<String, String>> transposed;
    private final String fixVersion;

    public FixTransposedTableModel(List<String> fixMessages) {
        this.columnHeaders = new ArrayList<>();
        this.transposed = new LinkedHashMap<>();

        String version = detectFixVersion(fixMessages.isEmpty() ? "" : fixMessages.get(0));
        this.fixVersion = version != null ? version : "FIX.4.2";

        Set<String> allTags = new LinkedHashSet<>();
        List<String> firstMessageTags = new ArrayList<>();
        int i = 1;

        for (String message : fixMessages) {
            String msgId = "Message " + i++;
            columnHeaders.add(msgId);
            Map<String, String> tagValueMap = parseFixMessage(message);

            // For the first message, capture tag order
            if (i == 2) { // i starts at 1, so first message
                firstMessageTags.addAll(tagValueMap.keySet());
            }

            for (String tag : tagValueMap.keySet()) {
                allTags.add(tag);
                transposed.computeIfAbsent(tag, k -> new LinkedHashMap<>()).put(msgId, tagValueMap.get(tag));
            }
        }

        // Ensure first message order first, then any extra tags
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
                .collect(Collectors.toMap(p -> p[0], p -> p[1], (a, b) -> b, LinkedHashMap::new)); // Maintain order
    }

    @Override
    public int getRowCount() {
        return tagOrder.size();
    }

    @Override
    public int getColumnCount() {
        return 2 + columnHeaders.size(); // Tag, Name, Messages
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) return "Tag";
        if (column == 1) return "Name";
        return columnHeaders.get(column - 2);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String tag = tagOrder.get(rowIndex);
        if (columnIndex == 0) return tag;
        if (columnIndex == 1) return FixTagDictionary.getTagName(fixVersion, tag);
        String msgId = columnHeaders.get(columnIndex - 2);
        return transposed.getOrDefault(tag, Collections.emptyMap()).getOrDefault(msgId, "");
    }
}
