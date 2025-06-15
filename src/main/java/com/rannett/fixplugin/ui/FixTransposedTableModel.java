package com.rannett.fixplugin.ui;

import com.intellij.openapi.project.Project;
import com.rannett.fixplugin.dictionary.FixDictionaryCache;
import com.rannett.fixplugin.dictionary.FixTagDictionary;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FixTransposedTableModel extends AbstractTableModel {
    private List<String> columnHeaders;
    private List<String> tagOrder;
    private Map<String, Map<String, String>> transposed;
    private String fixVersion;
    private final DocumentUpdater documentUpdater;
    private final Project project;

    private static class TagValue {
        final String tag;
        final String value;

        TagValue(String tag, String value) {
            this.tag = tag;
            this.value = value;
        }
    }

    public FixTransposedTableModel(List<String> fixMessages, DocumentUpdater updater, Project project) {
        this.documentUpdater = updater;
        this.project = project;
        buildModel(fixMessages);
    }

    public void updateMessages(List<String> fixMessages) {
        int oldRowCount = getRowCount();
        int oldColCount = getColumnCount();
        buildModel(fixMessages);
        SwingUtilities.invokeLater(() -> {
            if (getRowCount() != oldRowCount || getColumnCount() != oldColCount) fireTableStructureChanged();
            else fireTableDataChanged();
        });
    }

    private void buildModel(List<String> fixMessages) {
        columnHeaders = new ArrayList<>();
        transposed = new LinkedHashMap<>();
        String version = detectFixVersion(fixMessages.isEmpty() ? "" : fixMessages.get(0));
        fixVersion = version != null ? version : "FIXT.1.1";

        List<List<TagValue>> parsed = new ArrayList<>();
        // Maintain the order of tag occurrences as they first appear across all messages
        tagOrder = new ArrayList<>();
        Set<String> seenRows = new LinkedHashSet<>();

        int i = 1;
        for (String message : fixMessages) {
            // Preserve any \u0001 delimiters by avoiding trim().
            // Only strip Unicode whitespace from the ends.
            message = message.strip();
            if (message.isEmpty() || message.startsWith("#")) continue;
            String msgId = "Message " + i++;
            columnHeaders.add(msgId);

            List<TagValue> pairs = parseFixMessage(message);
            parsed.add(pairs);

            Map<String, Integer> counts = new LinkedHashMap<>();
            for (TagValue tv : pairs) {
                int occ = counts.merge(tv.tag, 1, Integer::sum);
                String rowId = tv.tag + "#" + occ;
                if (seenRows.add(rowId)) {
                    tagOrder.add(rowId);
                    transposed.put(rowId, new LinkedHashMap<>());
                }
            }
        }

        for (int idx = 0; idx < parsed.size(); idx++) {
            String msgId = columnHeaders.get(idx);
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (TagValue tv : parsed.get(idx)) {
                counts.merge(tv.tag, 1, Integer::sum);
                String rowId = tv.tag + "#" + counts.get(tv.tag);
                transposed.get(rowId).put(msgId, tv.value);
            }
        }
    }

    private String detectFixVersion(String message) {
        return com.rannett.fixplugin.util.FixUtils.extractFixVersion(message).orElse(null);
    }

    private List<TagValue> parseFixMessage(String msg) {
        return Arrays.stream(msg.split("[|\\u0001]"))
                .map(p -> p.split("=", 2))
                .filter(p -> p.length == 2)
                .map(p -> new TagValue(p[0], p[1]))
                .collect(Collectors.toList());
    }

    @Override
    public int getRowCount() {
        return tagOrder.size();
    }

    @Override
    public int getColumnCount() {
        return 2 + columnHeaders.size();
    }

    @Override
    public String getColumnName(int column) {
        return column == 0 ? "Tag" : column == 1 ? "Name" : columnHeaders.get(column - 2);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String rowId = tagOrder.get(rowIndex);
        String tag = rowId.contains("#") ? rowId.substring(0, rowId.indexOf('#')) : rowId;
        if (columnIndex == 0) return tag;
        if (columnIndex == 1) {
            FixTagDictionary dictionary = project.getService(FixDictionaryCache.class).getDictionary(fixVersion);
            String tagName = dictionary.getTagName(tag);
            return tagName != null ? tagName : "";  // Show empty string instead of null
        }
        String msgId = columnHeaders.get(columnIndex - 2);
        return transposed.getOrDefault(rowId, Collections.emptyMap()).getOrDefault(msgId, "");
    }


    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex >= 2;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String rowId = tagOrder.get(rowIndex);
        String tag = rowId.contains("#") ? rowId.substring(0, rowId.indexOf('#')) : rowId;
        int occurrence = 1;
        int hashIndex = rowId.indexOf('#');
        if (hashIndex >= 0) occurrence = Integer.parseInt(rowId.substring(hashIndex + 1));
        String msgId = columnHeaders.get(columnIndex - 2);
        String newValue = aValue.toString();
        transposed.get(rowId).put(msgId, newValue);
        documentUpdater.updateTagValueInMessage(msgId, tag, occurrence, newValue);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public int getRowForTag(String tag) {
        for (int i = 0; i < tagOrder.size(); i++) {
            if (getTagAtRow(i).equals(tag)) return i;
        }
        return -1;
    }

    public int getColumnForMessageId(String messageId) {
        int index = columnHeaders.indexOf(messageId);
        return index >= 0 ? index + 2 : -1;
    }

    public String getTagAtRow(int rowIndex) {
        String rowId = tagOrder.get(rowIndex);
        return rowId.contains("#") ? rowId.substring(0, rowId.indexOf('#')) : rowId;
    }

    public String getMessageIdForColumn(int columnIndex) {
        if (columnIndex < 2) return null;
        int modelIndex = columnIndex - 2;
        return modelIndex >= 0 && modelIndex < columnHeaders.size() ? columnHeaders.get(modelIndex) : null;
    }

    public String getFixVersion() {
        return fixVersion;
    }

    public interface DocumentUpdater {
        void updateTagValueInMessage(String messageId, String tag, int occurrence, String newValue);
    }
}
