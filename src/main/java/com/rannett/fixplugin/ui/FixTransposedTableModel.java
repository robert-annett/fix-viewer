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
import quickfix.field.EncodedSecurityDesc;
import quickfix.field.EncodedSecurityDescLen;
import quickfix.field.XmlData;
import quickfix.field.XmlDataLen;

public class FixTransposedTableModel extends AbstractTableModel {
    private List<String> columnHeaders;
    private List<String> tagOrder;
    private Map<String, Map<String, String>> transposed;
    private String fixVersion;
    private final DocumentUpdater documentUpdater;
    private final Project project;

    private record TagValue(String tag, String value) {
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
            if (getRowCount() != oldRowCount || getColumnCount() != oldColCount) {
                fireTableStructureChanged();
            } else {
                fireTableDataChanged();
            }
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
            if (message.isEmpty() || message.startsWith("#")) {
                continue;
            }
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

    /**
     * Parse a FIX message into tag/value pairs while preserving embedded data.
     * <p>
     * Certain FIX fields such as {@code XmlData.FIELD} and
     * {@code EncodedSecurityDesc.FIELD} may contain XML that itself includes
     * delimiter characters. These fields are preceded by a length tag
     * ({@code XmlDataLen.FIELD} or {@code EncodedSecurityDescLen.FIELD})
     * specifying how many characters to read. This method keeps
     * track of these lengths so that the embedded XML is extracted correctly.
     *
     * @param msg raw FIX message using either {@code |} or SOH as delimiters
     * @return ordered list of tag/value pairs from the message
     */
    private List<TagValue> parseFixMessage(String msg) {
        // Parsed output
        List<TagValue> result = new ArrayList<>();

        // Cursor into the raw message string
        int index = 0;

        // When a length field (XmlDataLen or EncodedSecurityDescLen) is encountered
        // the following tag will contain raw data of this length. 'expectedLength'
        // holds the number of characters to read and 'dataTag' stores the tag number
        // that should receive that data.
        int expectedLength = -1;
        String dataTag = null;

        // Iterate over the message: tag=value<delimiter>
        while (index < msg.length()) {
            // Find the '=' separating the tag from the value
            int eq = msg.indexOf('=', index);
            if (eq == -1) {
                break; // malformed message
            }

            String tag = msg.substring(index, eq);
            index = eq + 1;

            // If we previously saw a length tag, read the exact number of
            // characters for the current value regardless of delimiters.
            if (dataTag != null && tag.equals(dataTag) && expectedLength >= 0) {
                int end = Math.min(index + expectedLength, msg.length());
                String value = msg.substring(index, end);
                result.add(new TagValue(tag, value));

                // Move the cursor past the consumed data and optional delimiter
                index = end;
                // Some producers do not include trailing line breaks in the
                // length field. Skip over any whitespace before the delimiter
                while (index < msg.length() && Character.isWhitespace(msg.charAt(index))) {
                    index++;
                }
                if (index < msg.length() && (msg.charAt(index) == '|' || msg.charAt(index) == '\u0001')) {
                    index++;
                }

                // Reset special parsing state for the next iteration
                dataTag = null;
                expectedLength = -1;
                continue;
            }

            // Read a value terminated by the next delimiter character
            int delimPos = findDelimiter(msg, index);
            String value = msg.substring(index, delimPos);
            result.add(new TagValue(tag, value));
            index = delimPos + 1; // skip the delimiter

            // Check for length fields that signal the next tag contains raw data
            if (String.valueOf(XmlDataLen.FIELD).equals(tag)) { // XMLDataLen
                try {
                    expectedLength = Integer.parseInt(value);
                    dataTag = String.valueOf(XmlData.FIELD); // XMLData follows
                } catch (NumberFormatException ignore) {
                    // Invalid length values are treated as normal fields
                    expectedLength = -1;
                    dataTag = null;
                }
            } else if (String.valueOf(EncodedSecurityDescLen.FIELD).equals(tag)) { // EncodedSecurityDescLen
                try {
                    expectedLength = Integer.parseInt(value);
                    dataTag = String.valueOf(EncodedSecurityDesc.FIELD); // EncodedSecurityDesc follows
                } catch (NumberFormatException ignore) {
                    expectedLength = -1;
                    dataTag = null;
                }
            }
        }

        return result;
    }

    private int findDelimiter(String msg, int start) {
        int pipe = msg.indexOf('|', start);
        int soh = msg.indexOf('\u0001', start);
        int end = -1;
        if (pipe == -1) {
            end = soh;
        } else if (soh == -1) {
            end = pipe;
        } else {
            end = Math.min(pipe, soh);
        }
        if (end == -1) {
            end = msg.length();
        }
        return end;
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
        if (columnIndex == 0) {
            return tag;
        }
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
        if (hashIndex >= 0) {
            occurrence = Integer.parseInt(rowId.substring(hashIndex + 1));
        }
        String msgId = columnHeaders.get(columnIndex - 2);
        String newValue = aValue.toString();
        transposed.get(rowId).put(msgId, newValue);
        documentUpdater.updateTagValueInMessage(msgId, tag, occurrence, newValue);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public int getRowForTag(String tag) {
        for (int i = 0; i < tagOrder.size(); i++) {
            if (getTagAtRow(i).equals(tag)) {
                return i;
            }
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
        if (columnIndex < 2) {
            return null;
        }
        int modelIndex = columnIndex - 2;
        return modelIndex < columnHeaders.size() ? columnHeaders.get(modelIndex) : null;
    }

    public String getFixVersion() {
        return fixVersion;
    }

    /**
     * Triggers a repaint so that cached table rows pick up updated dictionary metadata.
     */
    public void refreshDictionaryMetadata() {
        SwingUtilities.invokeLater(() -> {
            if (getRowCount() > 0) {
                fireTableRowsUpdated(0, getRowCount() - 1);
            } else {
                fireTableDataChanged();
            }
        });
    }

    public interface DocumentUpdater {
        void updateTagValueInMessage(String messageId, String tag, int occurrence, String newValue);
    }
}
