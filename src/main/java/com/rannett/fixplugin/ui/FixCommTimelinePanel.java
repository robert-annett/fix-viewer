package com.rannett.fixplugin.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.ColumnInfo;
import com.rannett.fixplugin.settings.FixViewerSettingsState.DictionaryEntry;
import com.rannett.fixplugin.util.FixMessageParser;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import quickfix.DataDictionary;
import quickfix.Field;
import quickfix.FieldMap;
import quickfix.Group;
import quickfix.Message;

/**
 * Panel that renders a timeline view of FIX messages with an expandable
 * summary column.
 */
public class FixCommTimelinePanel extends JPanel {
    private static final String AUTO_COMP_ID = "Auto";

    private final JCheckBox hideHeartbeat;
    private final JComboBox<String> compIdSelector;
    private final List<MessageNode> allNodes = new ArrayList<>();
    private final List<MessageNode> displayedNodes = new ArrayList<>();
    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    private final ListTreeTableModelOnColumns model;
    private final TreeTable table;
    private Consumer<Integer> onMessageSelected;
    private boolean suppressCompIdChangeEvent;

    /**
     * Create a timeline panel for the provided messages.
     *
     * @param messages list of raw FIX messages
     */
    public FixCommTimelinePanel(@NotNull List<String> messages) {
        super(new BorderLayout());

        ColumnInfo[] columns = new ColumnInfo[]{
                new ColumnInfo<DefaultMutableTreeNode, String>("Time") {
                    @Override
                    public String valueOf(DefaultMutableTreeNode node) {
                        if (node instanceof MessageNode) {
                            return ((MessageNode) node).time;
                        }
                        return "";
                    }
                },
                new ColumnInfo<DefaultMutableTreeNode, String>("Dir") {
                    @Override
                    public String valueOf(DefaultMutableTreeNode node) {
                        if (node instanceof MessageNode) {
                            return ((MessageNode) node).direction;
                        }
                        return "";
                    }
                },
                new ColumnInfo<DefaultMutableTreeNode, String>("MsgType") {
                    @Override
                    public String valueOf(DefaultMutableTreeNode node) {
                        if (node instanceof MessageNode) {
                            return ((MessageNode) node).msgTypeDisplay;
                        }
                        return "";
                    }
                },
                new TreeColumnInfo("Summary")
        };

        model = new ListTreeTableModelOnColumns(root, columns);
        table = new TreeTable(model);
        table.setRootVisible(false);

        hideHeartbeat = new JCheckBox("Hide heartbeats");
        hideHeartbeat.addActionListener(e -> applyFilter());

        compIdSelector = new JComboBox<>();
        compIdSelector.setToolTipText("Direction is shown relative to selected CompID.");

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
        JLabel compIdLabel = new JLabel("Perspective CompID:");
        compIdLabel.setToolTipText("Direction is shown relative to selected CompID.");
        controls.add(compIdLabel);
        controls.add(compIdSelector);
        controls.add(new JLabel("   "));
        controls.add(hideHeartbeat);

        JScrollPane scroll = new JBScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(controls, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        Tree tree = table.getTree();
        tree.addTreeSelectionListener(e -> notifySelection());

        loadMessages(messages);
        compIdSelector.addActionListener(e -> {
            if (!suppressCompIdChangeEvent && ApplicationManager.getApplication() != null) {
                applyFilter();
            }
        });

        fixColumnWidths();
    }

    /**
     * Update the timeline with new messages.
     *
     * @param messages list of raw FIX messages
     */
    public void updateMessages(@NotNull List<String> messages) {
        loadMessages(messages);
    }

    /**
     * Configure a callback when a message row is selected.
     *
     * @param callback consumer receiving the 1-based message index
     */
    public void setOnMessageSelected(Consumer<Integer> callback) {
        this.onMessageSelected = callback;
    }

    /**
     * Set whether heartbeat messages should be hidden.
     *
     * @param hide {@code true} to hide heartbeats
     */
    public void setHideHeartbeats(boolean hide) {
        hideHeartbeat.setSelected(hide);
        applyFilter();
    }

    /**
     * Return the number of visible message rows.
     *
     * @return count of message nodes currently displayed
     */
    public int getVisibleRowCount() {
        return displayedNodes.size();
    }

    private void loadMessages(List<String> messages) {
        allNodes.clear();
        IntStream.range(0, messages.size()).forEach(i -> {
            MessageNode node = parseNode(messages.get(i), i + 1);
            allNodes.add(node);
        });
        refreshCompIdSelector();
        applyFilter();
    }

    private void applyFilter() {
        boolean ideApplicationReady = ApplicationManager.getApplication() != null;
        if (ideApplicationReady) {
            root.removeAllChildren();
        }
        displayedNodes.clear();
        allNodes.stream()
                .filter(n -> !hideHeartbeat.isSelected() || !"0".equals(n.msgTypeCode))
                .forEach(n -> {
                    n.direction = determineDirection(n.senderCompId, n.targetCompId);
                    displayedNodes.add(n);
                    if (ideApplicationReady) {
                        root.add(n);
                    }
                });
        if (ideApplicationReady) {
            model.reload();
        }
    }

    private void notifySelection() {
        TreePath path = table.getTree().getSelectionPath();
        if (path == null) {
            return;
        }
        Object node = path.getLastPathComponent();
        if (node instanceof MessageNode && onMessageSelected != null) {
            onMessageSelected.accept(((MessageNode) node).index);
        }
    }

    private MessageNode parseNode(String msg, int index) {
        String begin = extractBeginString(msg);
        try {
            DataDictionary dd = FixMessageParser.loadDataDictionary(begin, (DictionaryEntry) null);
            Message parsed = FixMessageParser.parse(msg, dd);

            String time = parsed.getHeader().isSetField(52) ? parsed.getHeader().getString(52) : "";
            String typeCode = parsed.getHeader().isSetField(35) ? parsed.getHeader().getString(35) : "";
            String typeName = buildTypeName(dd, typeCode);
            String sender = parsed.getHeader().isSetField(49) ? parsed.getHeader().getString(49) : "";
            String target = parsed.getHeader().isSetField(56) ? parsed.getHeader().getString(56) : "";
            String direction = determineDirection(sender, target);
            String summary = FixMessageParser.buildMessageLabel(parsed, dd);

            MessageNode node = new MessageNode(index, time, direction, typeCode, typeName, summary, sender, target);
            DefaultMutableTreeNode headerNode = new DefaultMutableTreeNode("Header");
            buildNodes(parsed.getHeader(), headerNode, dd);
            node.add(headerNode);

            DefaultMutableTreeNode bodyNode = new DefaultMutableTreeNode("Body");
            buildNodes(parsed, bodyNode, dd);
            node.add(bodyNode);

            DefaultMutableTreeNode trailerNode = new DefaultMutableTreeNode("Trailer");
            buildNodes(parsed.getTrailer(), trailerNode, dd);
            node.add(trailerNode);
            return node;
        } catch (Exception e) {
            MessageNode node = new MessageNode(index, "", "?", "", "", msg, "", "");
            node.add(new DefaultMutableTreeNode("Parse error: " + e.getMessage()));
            return node;
        }
    }

    private static String buildTypeName(DataDictionary dd, String typeCode) {
        if (typeCode == null || typeCode.isEmpty()) {
            return "";
        }
        String name = dd.getValueName(35, typeCode);
        if (name != null && !name.isEmpty()) {
            return typeCode + " (" + name.toUpperCase() + ")";
        }
        return typeCode;
    }

    private static String extractBeginString(String msg) {
        int start = msg.indexOf("8=");
        if (start >= 0) {
            int pipe = msg.indexOf('|', start);
            int soh = msg.indexOf('\u0001', start);
            int end = pipe >= 0 && (soh < 0 || pipe < soh) ? pipe : soh;
            if (end > start) {
                return msg.substring(start + 2, end);
            }
        }
        return "FIX.4.4";
    }

    private String determineDirection(String sender, String target) {
        if (sender.isEmpty() || target.isEmpty()) {
            return "?";
        }
        String selectedCompId = (String) compIdSelector.getSelectedItem();
        if (selectedCompId == null || AUTO_COMP_ID.equals(selectedCompId)) {
            selectedCompId = guessAutoCompId();
        }
        if (selectedCompId == null || selectedCompId.isEmpty()) {
            return "?";
        }
        if (selectedCompId.equals(sender)) {
            return "→";
        }
        if (selectedCompId.equals(target)) {
            return "←";
        }
        return "?";
    }

    private void refreshCompIdSelector() {
        Set<String> compIds = new HashSet<>();
        allNodes.forEach(node -> {
            if (!node.senderCompId.isEmpty()) {
                compIds.add(node.senderCompId);
            }
            if (!node.targetCompId.isEmpty()) {
                compIds.add(node.targetCompId);
            }
        });

        Object previous = compIdSelector.getSelectedItem();
        suppressCompIdChangeEvent = true;
        compIdSelector.removeAllItems();
        compIdSelector.addItem(AUTO_COMP_ID);
        compIds.stream().sorted().forEach(compIdSelector::addItem);
        if (previous != null && compIds.contains(previous.toString())) {
            compIdSelector.setSelectedItem(previous);
        } else {
            compIdSelector.setSelectedItem(AUTO_COMP_ID);
        }
        suppressCompIdChangeEvent = false;
    }

    private String guessAutoCompId() {
        return allNodes.stream()
                .flatMap(node -> java.util.stream.Stream.of(node.senderCompId, node.targetCompId))
                .filter(compId -> !compId.isEmpty())
                .collect(java.util.stream.Collectors.groupingBy(compId -> compId, java.util.stream.Collectors.counting()))
                .entrySet()
                .stream()
                .sorted((a, b) -> {
                    int countCompare = Long.compare(b.getValue(), a.getValue());
                    if (countCompare != 0) {
                        return countCompare;
                    }
                    return a.getKey().compareTo(b.getKey());
                })
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse("");
    }

    /**
     * Return the direction arrow for the given visible row.
     *
     * @param row zero-based index of the row
     * @return direction arrow or {@code null} if the row does not exist
     */
    String getDirectionAtRow(int row) {
        if (row < 0 || row >= displayedNodes.size()) {
            return null;
        }
        return displayedNodes.get(row).direction;
    }

    String getMsgTypeAtRow(int row) {
        if (row < 0 || row >= displayedNodes.size()) {
            return null;
        }
        return displayedNodes.get(row).msgTypeDisplay;
    }

    private void buildNodes(FieldMap map, DefaultMutableTreeNode parent, DataDictionary dd) {
        java.util.Iterator<Field<?>> fieldIt = map.iterator();
        while (fieldIt.hasNext()) {
            Field<?> field = fieldIt.next();
            int tag = field.getTag();
            String name = dd.getFieldName(tag);
            String value = String.valueOf(field.getObject());
            String enumName = dd.getValueName(tag, value);

            StringBuilder label = new StringBuilder();
            label.append(tag).append("=").append(value);
            if (name != null) {
                label.append(" (").append(name);
                if (enumName != null) {
                    label.append("=").append(enumName);
                }
                label.append(")");
            }
            parent.add(new DefaultMutableTreeNode(label.toString()));
        }

        java.util.Iterator<Integer> groupKeys = map.groupKeyIterator();
        while (groupKeys.hasNext()) {
            int groupTag = groupKeys.next();
            List<Group> groups = map.getGroups(groupTag);
            String groupName = dd.getFieldName(groupTag);
            int idx = 1;
            for (Group g : groups) {
                DefaultMutableTreeNode groupNode;
                String title;
                if (groupName != null) {
                    title = groupName;
                } else {
                    title = String.valueOf(groupTag);
                }
                groupNode = new DefaultMutableTreeNode(title + " [" + idx++ + "]");
                buildNodes(g, groupNode, dd);
                parent.add(groupNode);
            }
        }
    }

    private void fixColumnWidths() {
        TableColumn timeColumn = table.getColumnModel().getColumn(0);
        timeColumn.setMinWidth(150);
        timeColumn.setMaxWidth(150);
        timeColumn.setPreferredWidth(150);
        timeColumn.setResizable(false);

        TableColumn dirColumn = table.getColumnModel().getColumn(1);
        dirColumn.setMinWidth(40);
        dirColumn.setMaxWidth(40);
        dirColumn.setPreferredWidth(40);
        dirColumn.setResizable(false);
    }

    String getColumnName(int index) {
        return table.getColumnModel().getColumn(index).getHeaderValue().toString();
    }

    int getColumnPreferredWidth(int index) {
        return table.getColumnModel().getColumn(index).getPreferredWidth();
    }

    void setSelectedCompId(String compId) {
        suppressCompIdChangeEvent = true;
        compIdSelector.setSelectedItem(compId);
        suppressCompIdChangeEvent = false;
        applyFilter();
    }

    private static final class MessageNode extends DefaultMutableTreeNode {
        final int index;
        final String time;
        String direction;
        final String msgTypeCode;
        final String msgTypeDisplay;
        final String senderCompId;
        final String targetCompId;

        MessageNode(int index, String time, String direction, String msgTypeCode, String msgTypeDisplay, String summary, String senderCompId, String targetCompId) {
            super(summary);
            this.index = index;
            this.time = time;
            this.direction = direction;
            this.msgTypeCode = msgTypeCode;
            this.msgTypeDisplay = msgTypeDisplay;
            this.senderCompId = senderCompId;
            this.targetCompId = targetCompId;
        }
    }

}

