package com.rannett.fixplugin.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.Tree;
import com.rannett.fixplugin.util.FixMessageParser;
import com.rannett.fixplugin.util.FixUtils;
import com.rannett.fixplugin.settings.FixViewerSettingsState.DictionaryEntry;
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

    private JTree tree;

    private static final Logger LOG = Logger.getInstance(FixMessageTreePanel.class);
    private final Project project;
    private DictionaryEntry dictionaryEntry;

    public FixMessageTreePanel(List<String> fixMessages, Project project, DictionaryEntry dictionaryEntry) {
        super(new BorderLayout());
        this.project = project;
        this.dictionaryEntry = dictionaryEntry;
        buildTree(fixMessages);
    }

    public void updateTree(List<String> fixMessages) {
        buildTree(fixMessages);
    }

    public void setDictionaryEntry(DictionaryEntry dictionaryEntry) {
        this.dictionaryEntry = dictionaryEntry;
    }

    private void buildTree(List<String> fixMessages) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Messages");

        String fixVersion = detectFixVersion(fixMessages.isEmpty() ? "" : fixMessages.get(0));
        if (fixVersion == null) fixVersion = "FIXT.1.1";

        DataDictionary dd = FixMessageParser.loadDataDictionary(fixVersion, dictionaryEntry);

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
        setupTreeNavigation();

        removeAll();
        add(new JScrollPane(tree), BorderLayout.CENTER);
        revalidate();
    }

    private void setupTreeNavigation() {
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    navigateFromPath(tree.getPathForLocation(e.getX(), e.getY()));
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                maybeShow(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                maybeShow(e);
            }

            private void maybeShow(java.awt.event.MouseEvent e) {
                if (!e.isPopupTrigger() && !javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
                javax.swing.tree.TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path == null) {
                    return;
                }
                tree.setSelectionPath(path);
                Object node = path.getLastPathComponent();
                if (!(node instanceof javax.swing.tree.DefaultMutableTreeNode treeNode)) {
                    return;
                }
                String label = String.valueOf(treeNode.getUserObject());
                java.util.regex.Matcher tagMatcher = java.util.regex.Pattern.compile("^(\\d+)=").matcher(label);
                if (!tagMatcher.find()) {
                    return;
                }
                String tag = tagMatcher.group(1);
                String msgType = findMessageType(path);
                javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
                javax.swing.JMenuItem navigate = new javax.swing.JMenuItem("Go to Dictionary Definition");
                navigate.addActionListener(a -> FixDictionaryNavigator.navigateToTag(project, detectFixVersionFromTree(), msgType, tag));
                popup.add(navigate);
                popup.show(tree, e.getX(), e.getY());
            }

            private void navigateFromPath(javax.swing.tree.TreePath path) {
                if (path == null) {
                    return;
                }
                tree.setSelectionPath(path);
                Object node = path.getLastPathComponent();
                if (!(node instanceof javax.swing.tree.DefaultMutableTreeNode treeNode)) {
                    return;
                }
                String label = String.valueOf(treeNode.getUserObject());
                java.util.regex.Matcher tagMatcher = java.util.regex.Pattern.compile("^(\\d+)=").matcher(label);
                if (!tagMatcher.find()) {
                    return;
                }
                FixDictionaryNavigator.navigateToTag(project, detectFixVersionFromTree(), findMessageType(path), tagMatcher.group(1));
            }
        });
    }

    private String detectFixVersionFromTree() {
        return dictionaryEntry != null && dictionaryEntry.getVersion() != null ? dictionaryEntry.getVersion() : "FIXT.1.1";
    }

    private String findMessageType(javax.swing.tree.TreePath path) {
        Object[] nodes = path.getPath();
        for (Object node : nodes) {
            if (node instanceof javax.swing.tree.DefaultMutableTreeNode treeNode) {
                String label = String.valueOf(treeNode.getUserObject());
                if (!"Header".equals(label) && !"Body".equals(label) && !"Trailer".equals(label) && !"Messages".equals(label)) {
                    int space = label.indexOf(' ');
                    String token = space > 0 ? label.substring(0, space) : label;
                    if (!token.isBlank()) {
                        return token;
                    }
                }
            }
        }
        return null;
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

