package com.rannett.fixplugin;

import org.jetbrains.annotations.Nullable;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FixTagDictionary {
    private static final Map<String, Map<String, String>> tagNames = new HashMap<>();
    private static final Map<String, Map<String, Map<String, String>>> valueDescriptions = new HashMap<>();

    static {
        loadDictionary("FIX42.xml", "FIX.4.2");
        loadDictionary("FIX44.xml", "FIX.4.4");
        loadDictionary("FIX50.xml", "FIX.5.0");
    }

    private static void loadDictionary(String resourceName, String version) {
        try {
            InputStream is = FixTagDictionary.class.getClassLoader().getResourceAsStream("dictionaries/" + resourceName);
            if (is == null) throw new RuntimeException("Dictionary not found: dictionaries/" + resourceName);

            var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            var fieldNodes = doc.getElementsByTagName("field");

            Map<String, String> tagMap = new HashMap<>();
            Map<String, Map<String, String>> valueMap = new HashMap<>();

            for (int i = 0; i < fieldNodes.getLength(); i++) {
                var field = fieldNodes.item(i);
                var attrs = field.getAttributes();
                var number = attrs.getNamedItem("number");
                var name = attrs.getNamedItem("name");
                if (number != null && name != null) {
                    String tagNum = number.getNodeValue();
                    String tagName = name.getNodeValue();
                    tagMap.put(tagNum, tagName);

                    // Parse child <value> nodes
                    var childNodes = field.getChildNodes();
                    Map<String, String> tagValues = new HashMap<>();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        var child = childNodes.item(j);
                        if ("value".equals(child.getNodeName())) {
                            var childAttrs = child.getAttributes();
                            var enumAttr = childAttrs.getNamedItem("enum");
                            var descAttr = childAttrs.getNamedItem("description");
                            if (enumAttr != null && descAttr != null) {
                                tagValues.put(enumAttr.getNodeValue(), descAttr.getNodeValue());
                            }
                        }
                    }
                    if (!tagValues.isEmpty()) {
                        valueMap.put(tagNum, tagValues);
                    }
                }
            }

            tagNames.put(version, tagMap);
            valueDescriptions.put(version, valueMap);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load dictionary: " + resourceName, e);
        }
    }

    public static @Nullable String getTagName(String version, String tagNumber) {
        var versionMap = tagNames.get(version);
        if (versionMap == null) return null;
        return versionMap.get(tagNumber);
    }

    public static @Nullable String getValueName(String version, String tagNumber, String value) {
        var versionMap = valueDescriptions.get(version);
        if (versionMap == null) return null;
        var tagMap = versionMap.get(tagNumber);
        if (tagMap == null) return null;
        return tagMap.get(value);
    }

    public static @Nullable Map<String, String> getValueMap(String version, String tagNumber) {
        var versionMap = valueDescriptions.get(version);
        if (versionMap == null) return null;
        return versionMap.get(tagNumber);
    }
}
