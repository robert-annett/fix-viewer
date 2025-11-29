package com.rannett.fixplugin.ui.dictionary;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Builds a Swing {@link javax.swing.JTree} model representing a FIX dictionary.
 */
public class FixDictionaryTreeModelBuilder {

    private final String sourceLabel;

    /**
     * Creates a new builder that labels nodes with the provided source.
     *
     * @param sourceLabel dictionary source label
     */
    public FixDictionaryTreeModelBuilder(String sourceLabel) {
        this.sourceLabel = sourceLabel;
    }

    /**
     * Parses the provided input stream into a tree of {@link DefaultMutableTreeNode} instances.
     *
     * @param inputStream dictionary input
     * @return root tree node with child sections
     * @throws Exception if parsing fails
     */
    public DefaultMutableTreeNode buildTree(@NotNull InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputStream);

        Element root = document.getDocumentElement();
        Map<String, FieldDefinition> fieldsByName = parseFields(root);
        Map<String, Element> componentsByName = parseComponents(root);

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
                new FixDictionaryNodeData.Builder()
                        .withDisplayName("FIX Dictionary")
                        .withSource(sourceLabel)
                        .withType(FixDictionaryNodeData.NodeType.ROOT)
                        .build()
        );

        DefaultMutableTreeNode messagesNode = new DefaultMutableTreeNode(
                new FixDictionaryNodeData.Builder()
                        .withDisplayName("Messages")
                        .withType(FixDictionaryNodeData.NodeType.SECTION)
                        .withSource(sourceLabel)
                        .build());
        addMessages(root, messagesNode, fieldsByName, componentsByName);
        rootNode.add(messagesNode);

        DefaultMutableTreeNode componentsNode = new DefaultMutableTreeNode(
                new FixDictionaryNodeData.Builder()
                        .withDisplayName("Components")
                        .withType(FixDictionaryNodeData.NodeType.SECTION)
                        .withSource(sourceLabel)
                        .build());
        addComponents(componentsNode, componentsByName, fieldsByName);
        rootNode.add(componentsNode);

        DefaultMutableTreeNode fieldsNode = new DefaultMutableTreeNode(
                new FixDictionaryNodeData.Builder()
                        .withDisplayName("Fields")
                        .withType(FixDictionaryNodeData.NodeType.SECTION)
                        .withSource(sourceLabel)
                        .build());
        addFields(fieldsNode, fieldsByName);
        rootNode.add(fieldsNode);

        return rootNode;
    }

    private Map<String, FieldDefinition> parseFields(Element root) {
        Map<String, FieldDefinition> map = new LinkedHashMap<>();
        Element fields = firstChild(root, "fields");
        if (fields == null) {
            return map;
        }

        NodeList childNodes = fields.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (!(node instanceof Element element)) {
                continue;
            }
            if (!"field".equals(element.getTagName())) {
                continue;
            }

            String name = element.getAttribute("name");
            if (name == null || name.isEmpty()) {
                continue;
            }
            FieldDefinition definition = new FieldDefinition();
            definition.name = name;
            definition.number = element.getAttribute("number");
            definition.type = element.getAttribute("type");
            definition.description = element.getAttribute("description");
            definition.enums = parseEnums(element);
            map.put(name, definition);
        }

        return map;
    }

    private Map<String, Element> parseComponents(Element root) {
        Map<String, Element> map = new LinkedHashMap<>();
        Element components = firstChild(root, "components");
        if (components == null) {
            return map;
        }
        NodeList childNodes = components.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (!(node instanceof Element element)) {
                continue;
            }
            if (!"component".equals(element.getTagName())) {
                continue;
            }
            String name = element.getAttribute("name");
            if (name == null || name.isEmpty()) {
                continue;
            }
            map.put(name, element);
        }
        return map;
    }

    private void addMessages(Element root, DefaultMutableTreeNode messagesNode, Map<String, FieldDefinition> fieldsByName,
                             Map<String, Element> componentsByName) {
        Element header = firstChild(root, "header");
        Element trailer = firstChild(root, "trailer");
        Element messages = firstChild(root, "messages");
        if (messages == null) {
            return;
        }
        NodeList children = messages.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (!(node instanceof Element element)) {
                continue;
            }
            if (!"message".equals(element.getTagName())) {
                continue;
            }
            String name = element.getAttribute("name");
            String type = element.getAttribute("msgtype");
            String display = type != null && !type.isEmpty() ? type + " – " + name : name;

            FixDictionaryNodeData nodeData = new FixDictionaryNodeData.Builder()
                    .withType(FixDictionaryNodeData.NodeType.MESSAGE)
                    .withName(name)
                    .withDisplayName(display)
                    .withMessageType(type)
                    .withSource(sourceLabel)
                    .build();
            DefaultMutableTreeNode messageNode = new DefaultMutableTreeNode(nodeData);
            messagesNode.add(messageNode);

            if (header != null) {
                messageNode.add(buildSectionNode("Header", header, fieldsByName, componentsByName));
            }

            messageNode.add(buildSectionNode("Body", element, fieldsByName, componentsByName));

            if (trailer != null) {
                messageNode.add(buildSectionNode("Trailer", trailer, fieldsByName, componentsByName));
            }
        }
    }

    private void addComponents(DefaultMutableTreeNode parent, Map<String, Element> componentsByName,
                               Map<String, FieldDefinition> fieldsByName) {
        for (Map.Entry<String, Element> entry : componentsByName.entrySet()) {
            String name = entry.getKey();
            Element element = entry.getValue();

            DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(new FixDictionaryNodeData.Builder()
                    .withType(FixDictionaryNodeData.NodeType.COMPONENT)
                    .withName(name)
                    .withDisplayName("Component: " + name)
                    .withSource(sourceLabel)
                    .build());

            addMembers(componentNode, element, fieldsByName, componentsByName);
            parent.add(componentNode);
        }
    }

    private void addFields(DefaultMutableTreeNode parent, Map<String, FieldDefinition> fieldsByName) {
        for (FieldDefinition definition : fieldsByName.values()) {
            String display = definition.number != null && !definition.number.isEmpty()
                    ? definition.name + " (" + definition.number + ")"
                    : definition.name;
            FixDictionaryNodeData data = new FixDictionaryNodeData.Builder()
                    .withType(FixDictionaryNodeData.NodeType.FIELD)
                    .withName(definition.name)
                    .withDisplayName(display)
                    .withTagNumber(definition.number)
                    .withFixType(definition.type)
                    .withDescription(definition.description)
                    .withEnums(definition.enums)
                    .withSource(sourceLabel)
                    .build();
            parent.add(new DefaultMutableTreeNode(data));
        }
    }

    private DefaultMutableTreeNode buildSectionNode(String title, Element element,
                                                    Map<String, FieldDefinition> fieldsByName,
                                                    Map<String, Element> componentsByName) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new FixDictionaryNodeData.Builder()
                .withDisplayName(title)
                .withType(FixDictionaryNodeData.NodeType.SECTION)
                .withSource(sourceLabel)
                .build());
        addMembers(node, element, fieldsByName, componentsByName);
        return node;
    }

    private void addMembers(DefaultMutableTreeNode parent, Element container,
                            Map<String, FieldDefinition> fieldsByName, Map<String, Element> componentsByName) {
        NodeList childNodes = container.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (!(node instanceof Element element)) {
                continue;
            }
            switch (element.getTagName()) {
                case "field":
                    parent.add(createFieldNode(element, fieldsByName));
                    break;
                case "component":
                    parent.add(createComponentNode(element, fieldsByName, componentsByName));
                    break;
                case "group":
                    parent.add(createGroupNode(element, fieldsByName, componentsByName));
                    break;
                default:
                    break;
            }
        }
    }

    private DefaultMutableTreeNode createFieldNode(Element element, Map<String, FieldDefinition> fieldsByName) {
        String name = element.getAttribute("name");
        boolean required = "Y".equalsIgnoreCase(element.getAttribute("required"));
        FieldDefinition definition = fieldsByName.getOrDefault(name, new FieldDefinition());
        String display = definition.number != null && !definition.number.isEmpty()
                ? name + " (" + definition.number + ")"
                : name;
        FixDictionaryNodeData data = new FixDictionaryNodeData.Builder()
                .withType(FixDictionaryNodeData.NodeType.FIELD)
                .withName(name)
                .withDisplayName("Field: " + display)
                .withTagNumber(definition.number)
                .withFixType(definition.type)
                .withRequired(required)
                .withDescription(definition.description)
                .withEnums(definition.enums)
                .withSource(sourceLabel)
                .build();
        return new DefaultMutableTreeNode(data);
    }

    private DefaultMutableTreeNode createComponentNode(Element element, Map<String, FieldDefinition> fieldsByName,
                                                       Map<String, Element> componentsByName) {
        String name = element.getAttribute("name");
        boolean required = "Y".equalsIgnoreCase(element.getAttribute("required"));
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new FixDictionaryNodeData.Builder()
                .withType(FixDictionaryNodeData.NodeType.COMPONENT)
                .withName(name)
                .withDisplayName("Component: " + name)
                .withRequired(required)
                .withSource(sourceLabel)
                .build());
        Optional.ofNullable(componentsByName.get(name))
                .ifPresent(componentElement -> addMembers(node, componentElement, fieldsByName, componentsByName));
        return node;
    }

    private DefaultMutableTreeNode createGroupNode(Element element, Map<String, FieldDefinition> fieldsByName,
                                                   Map<String, Element> componentsByName) {
        String name = element.getAttribute("name");
        boolean required = "Y".equalsIgnoreCase(element.getAttribute("required"));
        FieldDefinition definition = fieldsByName.getOrDefault(name, new FieldDefinition());
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new FixDictionaryNodeData.Builder()
                .withType(FixDictionaryNodeData.NodeType.GROUP)
                .withName(name)
                .withDisplayName("Group: " + name)
                .withTagNumber(definition.number)
                .withRequired(required)
                .withSource(sourceLabel)
                .build());
        addMembers(node, element, fieldsByName, componentsByName);
        return node;
    }

    private Map<String, String> parseEnums(Element element) {
        Map<String, String> enums = new LinkedHashMap<>();
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (!(node instanceof Element child)) {
                continue;
            }
            if (!"value".equals(child.getTagName())) {
                continue;
            }
            String enumValue = child.getAttribute("enum");
            String description = child.getAttribute("description");
            if (enumValue != null && !enumValue.isEmpty()) {
                enums.put(enumValue, description);
            }
        }
        return enums;
    }

    private Element firstChild(Element root, String tag) {
        NodeList nodeList = root.getElementsByTagName(tag);
        if (nodeList.getLength() == 0) {
            return null;
        }
        Node node = nodeList.item(0);
        if (node instanceof Element element) {
            return element;
        }
        return null;
    }

    private static class FieldDefinition {
        private String name;
        private String number;
        private String type;
        private String description;
        private Map<String, String> enums = new LinkedHashMap<>();
    }
}
