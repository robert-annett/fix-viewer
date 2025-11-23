package com.rannett.fixplugin.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.Tree;
import com.rannett.fixplugin.util.DictionaryNavigationHelper;
import com.rannett.fixplugin.util.FixMessageParser;
import com.rannett.fixplugin.util.FixUtils;
import quickfix.DataDictionary;
import quickfix.Field;
import quickfix.FieldMap;
import quickfix.Group;
import quickfix.Message;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Iterator;
import java.util.List;

/**
 * Panel displaying FIX messages in a tree structure using the hierarchy
 * defined in the FIX dictionary.
 */
public class FixMessageTreePanel extends JPanel {

    private static final Logger LOG = Logger.getInstance(FixMessageTreePanel.class);
    private final Project project;
    private String fixVersion;
    private JTree tree;

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

        fixVersion = detectFixVersion(fixMessages.isEmpty() ? "" : fixMessages.get(0));
        if (fixVersion == null) fixVersion = "FIXT.1.1";
        this.fixVersion = fixVersion;

        DataDictionary dd = FixMessageParser.loadDataDictionary(this.fixVersion, project);

        for (String message : fixMessages) {
            message = message.strip();
            if (message.isEmpty() || message.startsWith("#")) continue;
            DefaultMutableTreeNode msgNode;
            try {
                Message qfMsg = FixMessageParser.parse(message, dd);
                String label = FixMessageParser.buildMessageLabel(qfMsg, dd);
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

        tree = new Tree(root);
        tree.setRootVisible(false);

        removeAll();
        add(new JScrollPane(tree), BorderLayout.CENTER);
        installContextMenu();
        revalidate();
    }

    private void installContextMenu() {
        tree.addMouseListener(new PopupHandler() {
            @Override
            public void invokePopup(Component comp, int x, int y) {
                TreePath path = tree.getPathForLocation(x, y);
                if (path == null) {
                    return;
                }
                tree.setSelectionPath(path);
                Object nodeObject = path.getLastPathComponent();
                if (!(nodeObject instanceof DefaultMutableTreeNode node)) {
                    return;
                }
                String label = String.valueOf(node.getUserObject());
                String tag = extractTag(label);
                if (tag == null) {
                    return;
                }
                String value = extractValue(label);
                JPopupMenu menu = new JPopupMenu();
                JMenuItem jumpToDictionary = new JMenuItem("Jump to Dictionary");
                jumpToDictionary.addActionListener(ae -> DictionaryNavigationHelper.jumpToDictionary(project, fixVersion, tag, value));
                menu.add(jumpToDictionary);
                menu.show(tree, x, y);
            }
        });
    }

    private String extractTag(String label) {
        int equalsIdx = label.indexOf('=');
        if (equalsIdx <= 0) {
            return null;
        }
        String candidate = label.substring(0, equalsIdx);
        for (int i = 0; i < candidate.length(); i++) {
            if (!Character.isDigit(candidate.charAt(i))) {
                return null;
            }
        }
        return candidate;
    }

    private String extractValue(String label) {
        int equalsIdx = label.indexOf('=');
        if (equalsIdx < 0) {
            return null;
        }
        int parenIdx = label.indexOf(' ', equalsIdx);
        if (parenIdx < 0) {
            parenIdx = label.indexOf('(', equalsIdx);
        }
        if (parenIdx < 0) {
            parenIdx = label.length();
        }
        return label.substring(equalsIdx + 1, parenIdx).trim();
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

