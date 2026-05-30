package com.rannett.fixplugin.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.StringReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.function.Consumer;

final class FixDictionaryTreePanel extends JPanel {
    private static final Logger LOG = Logger.getInstance(FixDictionaryTreePanel.class);
    private static final String MISSING_DICTIONARY_TEXT = "No dictionary messages found";

    private final Tree tree;
    private String dictionaryText;
    private MessageOrder messageOrder = MessageOrder.DICTIONARY;

    FixDictionaryTreePanel(@NotNull String dictionaryText, @NotNull Consumer<String> fieldNavigationHandler) {
        super(new BorderLayout());
        this.dictionaryText = dictionaryText;
        tree = createTree(dictionaryText, messageOrder);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && event.getButton() == MouseEvent.BUTTON1) {
                    handleDoubleClick(event, fieldNavigationHandler);
                }
            }
        });
        ToolTipManager.sharedInstance().registerComponent(tree);
        add(createOrderPanel(), BorderLayout.NORTH);
        add(ScrollPaneFactory.createScrollPane(tree), BorderLayout.CENTER);
    }

    void updateDictionaryText(@NotNull String dictionaryText) {
        this.dictionaryText = dictionaryText;
        refreshTreeModel();
    }

    private JPanel createOrderPanel() {
        JPanel orderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(8), 0));
        orderPanel.setBorder(JBUI.Borders.empty(4, 8));

        JBRadioButton dictionaryOrderButton = createOrderButton("Dictionary Order", MessageOrder.DICTIONARY);
        JBRadioButton nameOrderButton = createOrderButton("By Message Name", MessageOrder.NAME);
        JBRadioButton typeOrderButton = createOrderButton("By Message Type", MessageOrder.TYPE);
        dictionaryOrderButton.setSelected(true);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(dictionaryOrderButton);
        buttonGroup.add(nameOrderButton);
        buttonGroup.add(typeOrderButton);

        orderPanel.add(dictionaryOrderButton);
        orderPanel.add(nameOrderButton);
        orderPanel.add(typeOrderButton);
        return orderPanel;
    }

    private JBRadioButton createOrderButton(String label, MessageOrder order) {
        JBRadioButton button = new JBRadioButton(label);
        button.addActionListener(event -> {
            messageOrder = order;
            refreshTreeModel();
        });
        return button;
    }

    private void refreshTreeModel() {
        tree.setModel(buildModel(dictionaryText, messageOrder));
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
    }

    private static Tree createTree(String dictionaryText, MessageOrder messageOrder) {
        return new Tree(buildModel(dictionaryText, messageOrder)) {
            @Override
            public String getToolTipText(MouseEvent event) {
                TreePath path = getPathForLocation(event.getX(), event.getY());
                if (path == null) {
                    return null;
                }
                Object pathComponent = path.getLastPathComponent();
                if (!(pathComponent instanceof DefaultMutableTreeNode node)) {
                    return null;
                }
                Object userObject = node.getUserObject();
                if (userObject instanceof TreeNodePresentation presentation) {
                    return presentation.tooltip();
                }
                return null;
            }
        };
    }

    private void handleDoubleClick(MouseEvent event, Consumer<String> fieldNavigationHandler) {
        TreePath path = tree.getPathForLocation(event.getX(), event.getY());
        if (path == null) {
            return;
        }
        Object pathComponent = path.getLastPathComponent();
        if (!(pathComponent instanceof DefaultMutableTreeNode node)) {
            return;
        }
        Object userObject = node.getUserObject();
        if (userObject instanceof TreeNodePresentation presentation && presentation.fieldName() != null) {
            fieldNavigationHandler.accept(presentation.fieldName());
        }
    }

    @NotNull
    static DefaultTreeModel buildModel(@NotNull String dictionaryText) {
        return buildModel(dictionaryText, MessageOrder.DICTIONARY);
    }

    @NotNull
    static DefaultTreeModel buildModel(@NotNull String dictionaryText, @NotNull MessageOrder messageOrder) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(TreeNodePresentation.plain("Dictionary Messages"));
        try {
            Document document = parseDictionary(dictionaryText);
            Element dictionaryRoot = document.getDocumentElement();
            if (dictionaryRoot == null || !"fix".equalsIgnoreCase(dictionaryRoot.getTagName())) {
                root.add(new DefaultMutableTreeNode(TreeNodePresentation.plain(MISSING_DICTIONARY_TEXT)));
                return new DefaultTreeModel(root);
            }

            DictionaryIndex index = DictionaryIndex.create(dictionaryRoot);
            Element messages = firstDirectChild(dictionaryRoot, "messages");
            if (messages == null) {
                root.add(new DefaultMutableTreeNode(TreeNodePresentation.plain(MISSING_DICTIONARY_TEXT)));
                return new DefaultTreeModel(root);
            }

            orderedMessages(messages, messageOrder)
                    .map(message -> buildMessageNode(message, index))
                    .forEach(root::add);

            if (root.getChildCount() == 0) {
                root.add(new DefaultMutableTreeNode(TreeNodePresentation.plain(MISSING_DICTIONARY_TEXT)));
            }
        } catch (Exception exception) {
            LOG.warn("Failed to build FIX dictionary tree", exception);
            root.add(new DefaultMutableTreeNode(TreeNodePresentation.plain("Unable to parse dictionary XML")));
        }
        return new DefaultTreeModel(root);
    }

    private static java.util.stream.Stream<Element> orderedMessages(Element messages, MessageOrder messageOrder) {
        List<Element> messageElements = directChildren(messages, "message")
                .toList();
        Comparator<Element> comparator = switch (messageOrder) {
            case DICTIONARY -> null;
            case NAME -> Comparator.comparing(
                    element -> element.getAttribute("name"),
                    String.CASE_INSENSITIVE_ORDER
            );
            case TYPE -> Comparator.comparing(
                    element -> element.getAttribute("msgtype"),
                    String.CASE_INSENSITIVE_ORDER
            );
        };
        if (comparator == null) {
            return messageElements.stream();
        }
        return messageElements.stream().sorted(comparator);
    }

    private static Document parseDictionary(String dictionaryText) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(dictionaryText)));
    }

    private static DefaultMutableTreeNode buildMessageNode(Element message, DictionaryIndex index) {
        DefaultMutableTreeNode messageNode = new DefaultMutableTreeNode(TreeNodePresentation.plain(messageLabel(message)));
        addStructureChildren(message, messageNode, index, new HashSet<>());
        return messageNode;
    }

    private static void addStructureChildren(Element parentElement,
                                             DefaultMutableTreeNode parentNode,
                                             DictionaryIndex index,
                                             Set<String> componentPath) {
        directStructureChildren(parentElement)
                .map(child -> buildStructureNode(child, index, componentPath))
                .filter(Objects::nonNull)
                .forEach(parentNode::add);
    }

    private static DefaultMutableTreeNode buildStructureNode(Element element,
                                                            DictionaryIndex index,
                                                            Set<String> componentPath) {
        return switch (element.getTagName()) {
            case "field" -> buildFieldNode(element, index);
            case "group" -> buildGroupNode(element, index, componentPath);
            case "component" -> buildComponentNode(element, index, componentPath);
            default -> null;
        };
    }

    private static DefaultMutableTreeNode buildFieldNode(Element fieldReference, DictionaryIndex index) {
        String fieldName = fieldReference.getAttribute("name");
        Element fieldDefinition = index.fieldsByName.get(fieldReference.getAttribute("name"));
        return new DefaultMutableTreeNode(new TreeNodePresentation(
                fieldLabel(fieldReference, fieldDefinition),
                enumTooltip(fieldDefinition),
                fieldName
        ));
    }

    private static DefaultMutableTreeNode buildGroupNode(Element group,
                                                        DictionaryIndex index,
                                                        Set<String> componentPath) {
        DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(TreeNodePresentation.plain(groupLabel(group, index)));
        addStructureChildren(group, groupNode, index, new HashSet<>(componentPath));
        return groupNode;
    }

    private static DefaultMutableTreeNode buildComponentNode(Element componentReference,
                                                            DictionaryIndex index,
                                                            Set<String> componentPath) {
        String name = componentReference.getAttribute("name");
        DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(
                TreeNodePresentation.plain(componentLabel(componentReference)));
        if (name.isBlank()) {
            return componentNode;
        }
        Element componentDefinition = index.componentsByName.get(name);
        if (componentDefinition == null) {
            componentNode.add(new DefaultMutableTreeNode(TreeNodePresentation.plain("Component definition not found")));
            return componentNode;
        }
        if (componentPath.contains(name)) {
            componentNode.add(new DefaultMutableTreeNode(TreeNodePresentation.plain("Recursive component reference")));
            return componentNode;
        }

        Set<String> nestedPath = new HashSet<>(componentPath);
        nestedPath.add(name);
        addStructureChildren(componentDefinition, componentNode, index, nestedPath);
        return componentNode;
    }

    private static String messageLabel(Element message) {
        String name = message.getAttribute("name");
        String messageType = message.getAttribute("msgtype");
        String category = message.getAttribute("msgcat");
        String suffix = joinDetails(messageType, category);
        return suffix.isBlank() ? name : name + suffix;
    }

    private static String fieldLabel(Element fieldReference, Element fieldDefinition) {
        String name = fieldReference.getAttribute("name");
        String number = fieldDefinition != null ? fieldDefinition.getAttribute("number") : "";
        String type = fieldDefinition != null ? fieldDefinition.getAttribute("type") : "";
        return name + requiredSuffix(fieldReference) + joinDetails(number, type);
    }

    private static String enumTooltip(Element fieldDefinition) {
        if (fieldDefinition == null) {
            return null;
        }
        String values = directChildren(fieldDefinition, "value")
                .map(FixDictionaryTreePanel::enumTooltipLine)
                .collect(Collectors.joining("<br>"));
        if (values.isBlank()) {
            return null;
        }
        return "<html><b>Enum values</b><br>" + values + "</html>";
    }

    private static String enumTooltipLine(Element value) {
        String enumValue = value.getAttribute("enum");
        String description = value.getAttribute("description");
        if (description.isBlank()) {
            return htmlEscape(enumValue);
        }
        return htmlEscape(enumValue) + " - " + htmlEscape(description);
    }

    private static String htmlEscape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String groupLabel(Element group, DictionaryIndex index) {
        String name = group.getAttribute("name");
        Element fieldDefinition = index.fieldsByName.get(name);
        String number = fieldDefinition != null ? fieldDefinition.getAttribute("number") : "";
        return name + " group" + requiredSuffix(group) + joinDetails(number);
    }

    private static String componentLabel(Element component) {
        return component.getAttribute("name") + " component" + requiredSuffix(component);
    }

    private static String requiredSuffix(Element element) {
        String required = element.getAttribute("required");
        if ("Y".equalsIgnoreCase(required)) {
            return " [required]";
        }
        if ("N".equalsIgnoreCase(required)) {
            return " [optional]";
        }
        return "";
    }

    private static String joinDetails(String... details) {
        String joined = java.util.Arrays.stream(details)
                .filter(detail -> detail != null && !detail.isBlank())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        return joined.isBlank() ? "" : " (" + joined + ")";
    }

    private static Element firstDirectChild(Element parent, String tagName) {
        return directChildren(parent, tagName)
                .findFirst()
                .orElse(null);
    }

    private static java.util.stream.Stream<Element> directChildren(Element parent, String tagName) {
        return childElements(parent)
                .filter(element -> tagName.equals(element.getTagName()));
    }

    private static java.util.stream.Stream<Element> directStructureChildren(Element parent) {
        return childElements(parent)
                .filter(element -> "field".equals(element.getTagName())
                        || "group".equals(element.getTagName())
                        || "component".equals(element.getTagName()));
    }

    private static java.util.stream.Stream<Element> childElements(Element parent) {
        return IntStream.range(0, parent.getChildNodes().getLength())
                .mapToObj(parent.getChildNodes()::item)
                .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
                .map(node -> (Element) node);
    }

    private static final class DictionaryIndex {
        private final Map<String, Element> fieldsByName;
        private final Map<String, Element> componentsByName;

        private DictionaryIndex(Map<String, Element> fieldsByName, Map<String, Element> componentsByName) {
            this.fieldsByName = fieldsByName;
            this.componentsByName = componentsByName;
        }

        private static DictionaryIndex create(Element dictionaryRoot) {
            Map<String, Element> fieldsByName = new HashMap<>();
            Map<String, Element> componentsByName = new HashMap<>();

            Element fields = firstDirectChild(dictionaryRoot, "fields");
            if (fields != null) {
                directChildren(fields, "field")
                        .forEach(field -> fieldsByName.put(field.getAttribute("name"), field));
            }

            Element components = firstDirectChild(dictionaryRoot, "components");
            if (components != null) {
                directChildren(components, "component")
                        .forEach(component -> componentsByName.put(component.getAttribute("name"), component));
            }

            return new DictionaryIndex(fieldsByName, componentsByName);
        }
    }

    static final class TreeNodePresentation {
        private final String label;
        private final String tooltip;
        private final String fieldName;

        private TreeNodePresentation(String label, String tooltip, String fieldName) {
            this.label = label;
            this.tooltip = tooltip;
            this.fieldName = fieldName;
        }

        private static TreeNodePresentation plain(String label) {
            return new TreeNodePresentation(label, null, null);
        }

        String tooltip() {
            return tooltip;
        }

        String fieldName() {
            return fieldName;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    enum MessageOrder {
        DICTIONARY,
        NAME,
        TYPE
    }
}
