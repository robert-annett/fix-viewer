package com.rannett.fixplugin.ui.dictionary;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Split-pane panel showing the dictionary tree alongside details of the selected entry.
 */
public class FixDictionaryTreePanel extends JPanel {

    private final JPanel detailPanel;
    private final FixDictionaryTreeModelBuilder modelBuilder;

    /**
     * Creates a new dictionary tree panel.
     *
     * @param sourceLabel label displayed as the dictionary source
     */
    public FixDictionaryTreePanel(String sourceLabel) {
        super(new BorderLayout());
        this.modelBuilder = new FixDictionaryTreeModelBuilder(sourceLabel);
        this.detailPanel = new JPanel(new BorderLayout());
        this.detailPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    /**
     * Builds the UI using the provided dictionary stream.
     *
     * @param inputStream dictionary content
     * @throws Exception when parsing fails
     */
    public void build(@NotNull java.io.InputStream inputStream) throws Exception {
        DefaultMutableTreeNode rootNode = modelBuilder.buildTree(inputStream);
        JTree tree = new Tree(rootNode);
        tree.setRootVisible(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new DictionaryTreeCellRenderer());
        tree.addTreeSelectionListener(event -> {
            Object lastPathComponent = event.getPath().getLastPathComponent();
            if (lastPathComponent instanceof DefaultMutableTreeNode node) {
                Object userObject = node.getUserObject();
                if (userObject instanceof FixDictionaryNodeData data) {
                    renderDetails(data);
                }
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JBScrollPane(tree), new JBScrollPane(detailPanel));
        splitPane.setResizeWeight(0.35);

        removeAll();
        add(splitPane, BorderLayout.CENTER);
        revalidate();
        tree.setSelectionRow(0);
    }

    private void renderDetails(FixDictionaryNodeData data) {
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(4, 4, 4, 4);

        addRow(content, constraints, "Name", data.getDisplayName());
        addRow(content, constraints, "Kind", formatKind(data));
        addRow(content, constraints, "Tag", nullable(data.getTagNumber()));
        addRow(content, constraints, "Type", nullable(data.getFixType()));
        addRow(content, constraints, "MsgType", nullable(data.getMessageType()));
        addRow(content, constraints, "Required", data.isRequired() ? "Yes" : "No");
        addRow(content, constraints, "Description", nullable(data.getDescription()));
        addRow(content, constraints, "Source", nullable(data.getSource()));

        if (!data.getEnums().isEmpty()) {
            constraints.gridx = 0;
            constraints.gridy++;
            constraints.gridwidth = 2;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            JBLabel label = new JBLabel("Enumerations:");
            label.setFont(label.getFont().deriveFont(label.getFont().getStyle() | java.awt.Font.BOLD));
            content.add(label, constraints);

            List<String> items = new ArrayList<>();
            for (Map.Entry<String, String> entry : data.getEnums().entrySet()) {
                String description = entry.getValue() != null ? entry.getValue() : "";
                items.add(entry.getKey() + " - " + description);
            }
            JBList<String> enumList = new JBList<>(items);
            enumList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            constraints.gridy++;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.fill = GridBagConstraints.BOTH;
            content.add(new JBScrollPane(enumList), constraints);
        }

        detailPanel.removeAll();
        detailPanel.add(content, BorderLayout.NORTH);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void addRow(JPanel panel, GridBagConstraints constraints, String label, String value) {
        JBLabel fieldLabel = new JBLabel(label + ":");
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        panel.add(fieldLabel, constraints);

        JBLabel valueLabel = new JBLabel(value);
        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(valueLabel, constraints);
        constraints.gridy++;
    }

    private String formatKind(FixDictionaryNodeData data) {
        String base = data.getType().name().toLowerCase();
        return base.substring(0, 1).toUpperCase() + base.substring(1);
    }

    private String nullable(String value) {
        if (value == null || value.isEmpty()) {
            return "—";
        }
        return value;
    }

    private static class DictionaryTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public java.awt.Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                               boolean leaf, int row, boolean hasFocus) {
            java.awt.Component component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (!(value instanceof DefaultMutableTreeNode node)) {
                return component;
            }
            Object userObject = node.getUserObject();
            if (userObject instanceof FixDictionaryNodeData data) {
                setIcon(resolveIcon(data));
                setText(Objects.requireNonNullElse(data.getDisplayName(), ""));
            }
            return component;
        }

        private javax.swing.Icon resolveIcon(FixDictionaryNodeData data) {
            return switch (data.getType()) {
                case MESSAGE -> AllIcons.Nodes.Class;
                case COMPONENT -> AllIcons.Nodes.Method;
                case GROUP -> AllIcons.Nodes.Folder;
                case FIELD -> AllIcons.Nodes.Property;
                default -> AllIcons.Nodes.Package;
            };
        }
    }
}
