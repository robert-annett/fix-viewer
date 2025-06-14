package com.rannett.fixplugin.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.rannett.fixplugin.settings.FixViewerSettingsState;
import com.rannett.fixplugin.util.FixUtils;
import org.jetbrains.annotations.NotNull;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.Field;
import quickfix.FieldMap;
import quickfix.Group;
import quickfix.Message;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.BorderLayout;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Panel displaying FIX messages in a tree structure using the hierarchy
 * defined in the FIX dictionary.
 */
public class FixMessageTreePanel extends JPanel {

    private static final Logger LOG = Logger.getInstance(FixMessageTreePanel.class);
    private JTree tree;
    private String fixVersion;
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

        fixVersion = detectFixVersion(fixMessages.isEmpty() ? "" : fixMessages.get(0));
        if (fixVersion == null) fixVersion = "FIXT.1.1";

        DataDictionary dd = loadDataDictionary(fixVersion);

        int i = 1;
        for (String message : fixMessages) {
            message = message.trim();
            if (message.isEmpty() || message.startsWith("#")) continue;
            String msgId = "Message " + i++;
            DefaultMutableTreeNode msgNode = new DefaultMutableTreeNode(msgId);
            try {
                Message qfMsg = new Message();
                qfMsg.fromString(message, dd, true);

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
                LOG.warn("Failed to parse FIX message: " + message, e);
                msgNode.add(new DefaultMutableTreeNode("Parse error: " + e.getMessage()));
            }
            root.add(msgNode);
        }

        tree = new JTree(root);
        tree.setRootVisible(false);

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
            parent.add(new DefaultMutableTreeNode(tag + "=" + value + (name != null ? " (" + name + ")" : "")));
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

    private DataDictionary loadDataDictionary(@NotNull String version) {
        FixViewerSettingsState settings = FixViewerSettingsState.getInstance(project);
        String customPath = settings.getCustomDictionaryPath(version);
        try {
            if (customPath != null && !customPath.isEmpty()) {
                return new DataDictionary(new File(customPath).getAbsolutePath());
            }
            String resourcePath = "/dictionaries/" + version + ".xml";
            InputStream stream = FixMessageTreePanel.class.getResourceAsStream(resourcePath);
            if (stream != null) {
                return new DataDictionary(stream);
            }
            return new DataDictionary(version);
        } catch (ConfigError e) {
            throw new RuntimeException("Failed to load dictionary for " + version, e);
        }
    }

    public JComponent getTreeComponent() {
        return tree;
    }
}

