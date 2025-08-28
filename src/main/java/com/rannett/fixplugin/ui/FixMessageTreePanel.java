package com.rannett.fixplugin.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.Tree;
import com.rannett.fixplugin.util.FixMessageParser;
import com.rannett.fixplugin.util.FixUtils;
import com.rannett.fixplugin.dictionary.FixDictionaryCache;
import com.rannett.fixplugin.dictionary.FixTagDictionary;
import com.rannett.fixplugin.settings.FixViewerSettingsState;
import quickfix.DataDictionary;
import quickfix.Field;
import quickfix.FieldMap;
import quickfix.Group;
import quickfix.Message;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.List;

/**
 * Panel displaying FIX messages in a tree structure using the hierarchy
 * defined in the FIX dictionary.
 */
public class FixMessageTreePanel extends JPanel {

    private static final Logger LOG = Logger.getInstance(FixMessageTreePanel.class);
    private final Project project;

    public FixMessageTreePanel(List<String> fixMessages, Project project) {
        super(new BorderLayout());
        this.project = project;
        buildTree(fixMessages);
    }

    public void updateTree(List<String> fixMessages) {
        buildTree(fixMessages);
    }

    private void buildTree(List<String> fixMessages) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Messages");

        String fixVersion = detectFixVersion(fixMessages.isEmpty() ? "" : fixMessages.get(0));
        if (fixVersion == null) fixVersion = "FIXT.1.1";

        DataDictionary dd = FixMessageParser.loadDataDictionary(fixVersion, project);
        FixViewerSettingsState settings = FixViewerSettingsState.getInstance(project);
        java.util.List<Integer> headerFields = settings.getHeaderFieldList();
        FixTagDictionary tagDictionary = project.getService(FixDictionaryCache.class).getDictionary(fixVersion);

        for (String message : fixMessages) {
            message = message.strip();
            if (message.isEmpty() || message.startsWith("#")) continue;
            DefaultMutableTreeNode msgNode;
            try {
                Message qfMsg = FixMessageParser.parse(message, dd);
                String label = FixMessageParser.buildMessageLabel(qfMsg, dd, headerFields);
                msgNode = new DefaultMutableTreeNode(label);

                DefaultMutableTreeNode headerNode = new DefaultMutableTreeNode("Header");
                buildNodes(qfMsg.getHeader(), headerNode, dd);
                msgNode.add(headerNode);

                DefaultMutableTreeNode bodyNode = new DefaultMutableTreeNode("Body");
                buildNodes(qfMsg, bodyNode, dd);
                msgNode.add(bodyNode);

                DefaultMutableTreeNode trailerNode = new DefaultMutableTreeNode("Trailer");
                buildNodes(qfMsg.getTrailer(), trailerNode, dd);
                msgNode.add(trailerNode);

            } catch (Exception e) {
                msgNode = new DefaultMutableTreeNode("Parse error");
                LOG.warn("Failed to parse FIX message: " + message, e);
                msgNode.add(new DefaultMutableTreeNode("Parse error: " + e.getMessage()));
            }
            root.add(msgNode);
        }

        JTree tree = new Tree(root);
        tree.setRootVisible(false);
        tree.setCellRenderer(new javax.swing.tree.DefaultTreeCellRenderer() {
            @Override
            public java.awt.Component getTreeCellRendererComponent(JTree t, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                java.awt.Component c = super.getTreeCellRendererComponent(t, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode node) {
                    Object obj = node.getUserObject();
                    if (obj instanceof String str && str.contains("=")) {
                        String tagPart = str.substring(0, str.indexOf('='));
                        String digits = tagPart.replaceAll("[^0-9]", "");
                        String type = tagDictionary.getFieldType(digits);
                        java.awt.Color col = settings.getColorForFieldType(type);
                        if (!sel && col != null) {
                            setForeground(col);
                        } else {
                            setForeground(javax.swing.UIManager.getColor("Tree.textForeground"));
                        }
                    }
                }
                return c;
            }
        });

        removeAll();
        add(new JScrollPane(tree), BorderLayout.CENTER);
        revalidate();
    }

    private void buildNodes(FieldMap map, DefaultMutableTreeNode parent, DataDictionary dd) {
        Iterator<Field<?>> fieldIt = map.iterator();
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

        Iterator<Integer> groupKeys = map.groupKeyIterator();
        while (groupKeys.hasNext()) {
            int groupTag = groupKeys.next();
            List<Group> groups = map.getGroups(groupTag);
            String groupName = dd.getFieldName(groupTag);
            int idx = 1;
            for (Group g : groups) {
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(
                        (groupName != null ? groupName : groupTag) + " [" + idx++ + "]");
                buildNodes(g, groupNode, dd);
                parent.add(groupNode);
            }
        }
    }

    private String detectFixVersion(String message) {
        return FixUtils.extractFixVersion(message).orElse(null);
    }

}

