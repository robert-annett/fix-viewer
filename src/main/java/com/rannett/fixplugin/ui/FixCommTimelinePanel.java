package com.rannett.fixplugin.ui;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Panel that renders a timeline view of FIX messages with an expandable
 * summary column.
 */
public class FixCommTimelinePanel extends JPanel {
    private final JCheckBox hideHeartbeat;
    private final List<FixTimelineMessageNode> allNodes = new ArrayList<>();
    private final List<FixTimelineMessageNode> displayedNodes = new ArrayList<>();
    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    private final ListTreeTableModelOnColumns model;
    private final TreeTable table;
    private final FixTimelineNodeBuilder nodeBuilder = new FixTimelineNodeBuilder();
    private Consumer<Integer> onMessageSelected;

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
                        if (node instanceof FixTimelineMessageNode) {
                            return ((FixTimelineMessageNode) node).time;
                        }
                        return "";
                    }
                },
                new ColumnInfo<DefaultMutableTreeNode, String>("Dir") {
                    @Override
                    public String valueOf(DefaultMutableTreeNode node) {
                        if (node instanceof FixTimelineMessageNode) {
                            return ((FixTimelineMessageNode) node).direction;
                        }
                        return "";
                    }
                },
                new ColumnInfo<DefaultMutableTreeNode, String>("MsgType") {
                    @Override
                    public String valueOf(DefaultMutableTreeNode node) {
                        if (node instanceof FixTimelineMessageNode) {
                            return ((FixTimelineMessageNode) node).msgTypeDisplay;
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

        JScrollPane scroll = new JBScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(hideHeartbeat, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        Tree tree = table.getTree();
        tree.addTreeSelectionListener(e -> notifySelection());

        loadMessages(messages);

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
        nodeBuilder.resetSessionParties();
        IntStream.range(0, messages.size()).forEach(i -> {
            FixTimelineMessageNode node = nodeBuilder.buildNode(messages.get(i), i + 1);
            allNodes.add(node);
        });
        applyFilter();
    }

    private void applyFilter() {
        root.removeAllChildren();
        displayedNodes.clear();
        allNodes.stream()
                .filter(n -> !hideHeartbeat.isSelected() || !"0".equals(n.msgTypeCode))
                .forEach(n -> {
                    displayedNodes.add(n);
                    root.add(n);
                });
        model.setRoot(root);
    }

    private void notifySelection() {
        TreePath path = table.getTree().getSelectionPath();
        if (path == null) {
            return;
        }
        Object node = path.getLastPathComponent();
        if (node instanceof FixTimelineMessageNode && onMessageSelected != null) {
            onMessageSelected.accept(((FixTimelineMessageNode) node).index);
        }
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

}
