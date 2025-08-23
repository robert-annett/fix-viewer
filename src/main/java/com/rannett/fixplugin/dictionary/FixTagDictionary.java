package com.rannett.fixplugin.dictionary;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FixTagDictionary {

    private static final Logger LOG = Logger.getInstance(FixTagDictionary.class);

    private final Map<String, String> tagNameMap = new HashMap<>();
    private final Map<String, Map<String, String>> tagValueMap = new HashMap<>();
    private final Map<String, String> fieldTypeMap = new HashMap<>();
    private final Map<String, FieldSection> fieldSectionMap = new HashMap<>();
    private final Map<String, String> fieldDescriptionMap = new HashMap<>();

    FixTagDictionary() {
    }

    /**
     * Creates a {@link FixTagDictionary} from a JSON or XML file.
     *
     * @param file the dictionary file
     * @return populated dictionary instance
     */
    public static FixTagDictionary fromFile(@NotNull File file) {
        FixTagDictionary dictionary = new FixTagDictionary();
        String fileName = file.getName().toLowerCase();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            if (fileName.endsWith(".json")) {
                parseJson(reader, dictionary);
            } else if (fileName.endsWith(".xml")) {
                parseXml(reader, dictionary);
            } else {
                LOG.warn("Unsupported file format: " + fileName);
            }
        } catch (Exception e) {
            LOG.warn("Failed to load dictionary file: " + fileName, e);
        }

        loadFieldDescriptions(dictionary);

        return dictionary;
    }

    /**
     * Loads a built-in FIX dictionary by version.
     *
     * @param version FIX version identifier, e.g. {@code "FIX.4.4"}
     * @return populated dictionary instance
     */
    public static FixTagDictionary fromBuiltInVersion(@NotNull String version) {
        FixTagDictionary dictionary = new FixTagDictionary();

        String resourcePath = "/dictionaries/" + version + ".xml";
        try (InputStream inputStream = FixTagDictionary.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                LOG.warn("Built-in dictionary not found: " + version);
                return dictionary;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                parseXml(reader, dictionary);
            } catch (Exception e) {
                LOG.warn("Failed to load built-in dictionary: " + version, e);
            }
        } catch (Exception e) {
            LOG.warn("Failed to load built-in dictionary: " + version, e);
        }

        loadFieldDescriptions(dictionary);

        return dictionary;
    }

    /**
     * Retrieves the field name for a given tag number.
     *
     * @param tag numeric tag identifier
     * @return field name or {@code null} if unknown
     */
    public String getTagName(String tag) {
        return tagNameMap.get(tag);
    }

    /**
     * Looks up the description for a specific enumerated value of a tag.
     *
     * @param tag   numeric tag identifier
     * @param value enumerated value
     * @return description or {@code null} if not defined
     */
    public String getValueName(String tag, String value) {
        Map<String, String> valueMap = tagValueMap.get(tag);
        return valueMap != null ? valueMap.get(value) : null;
    }

    /**
     * Returns the FIX field type for the given tag.
     *
     * @param tag numeric tag identifier
     * @return field type or {@code null} if unknown
     */
    public String getFieldType(String tag) {
        return fieldTypeMap.get(tag);
    }

    /**
     * Provides an unmodifiable view of the tag-to-name map.
     *
     * @return mapping of tag numbers to names
     */
    public Map<String, String> getTagNameMap() {
        return Collections.unmodifiableMap(tagNameMap);
    }

    /**
     * Retrieves the section of the FIX message where the tag appears.
     *
     * @param tag numeric tag identifier
     * @return the section classification or {@code null} if unknown
     */
    public FieldSection getFieldSection(String tag) {
        return fieldSectionMap.get(tag);
    }

    /**
     * Retrieves the description text for a given tag number.
     *
     * @param tag numeric tag identifier
     * @return description text or {@code null} if unavailable
     */
    public String getFieldDescription(String tag) {
        return fieldDescriptionMap.get(tag);
    }

    private static void parseJson(BufferedReader reader, FixTagDictionary dictionary) throws Exception {
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }

        String jsonContent = jsonBuilder.toString();
        org.json.JSONObject root = new org.json.JSONObject(jsonContent);

        for (String key : root.keySet()) {
            Object value = root.get(key);
            if (value instanceof String) {
                dictionary.tagNameMap.put(key, (String) value);
            } else if (value instanceof org.json.JSONObject fieldObject) {

                if (fieldObject.has("name")) {
                    dictionary.tagNameMap.put(key, fieldObject.getString("name"));
                }
                if (fieldObject.has("type")) {
                    dictionary.fieldTypeMap.put(key, fieldObject.getString("type")); // Add field type
                }

                org.json.JSONObject valuesObject = fieldObject.optJSONObject("values");
                if (valuesObject != null) {
                    Map<String, String> valueMap = new HashMap<>();
                    for (String valKey : valuesObject.keySet()) {
                        valueMap.put(valKey, valuesObject.getString(valKey));
                    }
                    dictionary.tagValueMap.put(key, valueMap);
                }
            }
        }
    }

    private static void parseXml(BufferedReader reader, FixTagDictionary dictionary) throws Exception {
        String line;
        String currentTag = null;
        boolean inHeader = false;
        boolean inTrailer = false;
        Set<String> headerNames = new HashSet<>();
        Set<String> trailerNames = new HashSet<>();

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if ("<header>".equals(line)) {
                inHeader = true;
                continue;
            }
            if ("</header>".equals(line)) {
                inHeader = false;
                continue;
            }
            if ("<trailer>".equals(line)) {
                inTrailer = true;
                continue;
            }
            if ("</trailer>".equals(line)) {
                inTrailer = false;
                continue;
            }
            if ((inHeader || inTrailer) && line.startsWith("<field")) {
                String nameAttr = extractAttribute(line, "name");
                if (nameAttr != null) {
                    if (inHeader) {
                        headerNames.add(nameAttr);
                    } else {
                        trailerNames.add(nameAttr);
                    }
                }
                continue;
            }
            if (line.startsWith("<field")) {
                String tagNumber = extractAttribute(line, "number");
                String tagName = extractAttribute(line, "name");
                String type = extractAttribute(line, "type");
                if (tagNumber != null && tagName != null) {
                    dictionary.tagNameMap.put(tagNumber, tagName);
                    currentTag = tagNumber;
                    if (headerNames.contains(tagName)) {
                        dictionary.fieldSectionMap.put(tagNumber, FieldSection.HEADER);
                    } else if (trailerNames.contains(tagName)) {
                        dictionary.fieldSectionMap.put(tagNumber, FieldSection.TRAILER);
                    } else {
                        dictionary.fieldSectionMap.put(tagNumber, FieldSection.BODY);
                    }
                }
                if (tagNumber != null && type != null) {
                    dictionary.fieldTypeMap.put(tagNumber, type);
                }
            } else if (line.startsWith("<value") && currentTag != null) {
                String enumValue = extractAttribute(line, "enum");
                String description = extractAttribute(line, "description");
                if (enumValue != null && description != null) {
                    dictionary.tagValueMap
                            .computeIfAbsent(currentTag, k -> new HashMap<>())
                            .put(enumValue, description);
                }
            }
        }
    }

    private static String extractAttribute(String line, String attributeName) {
        String search = attributeName + "=\"";
        int start = line.indexOf(search);
        if (start < 0) {
            return null;
        }
        start += search.length();
        int end = line.indexOf("\"", start);
        if (end < 0) {
            return null;
        }
        return line.substring(start, end);
    }

    /**
     * Provides all enumerated values for a given tag.
     *
     * @param currentTag numeric tag identifier
     * @return mapping of value to description or {@code null} if none defined
     */
    public Map<String, String> getValueMap(String currentTag) {
        return tagValueMap.get(currentTag);
    }

    private static void loadFieldDescriptions(FixTagDictionary dictionary) {
        String path = "/documentation/FIX.5.0SP2_en_phrases.xml";
        try (InputStream inputStream = FixTagDictionary.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                LOG.warn("Phrases file not found: " + path);
                return;
            }
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(inputStream);
            org.w3c.dom.NodeList phrases = document.getElementsByTagName("phrase");
            java.util.stream.IntStream.range(0, phrases.getLength())
                    .mapToObj(phrases::item)
                    .filter(node -> node instanceof org.w3c.dom.Element)
                    .map(node -> (org.w3c.dom.Element) node)
                    .filter(element -> {
                        String textId = element.getAttribute("textId");
                        return textId != null && textId.startsWith("FIELD_");
                    })
                    .forEach(element -> {
                        String textId = element.getAttribute("textId");
                        String tag = textId.substring("FIELD_".length());
                        org.w3c.dom.NodeList paras = element.getElementsByTagName("para");
                        String description = java.util.stream.IntStream.range(0, paras.getLength())
                                .mapToObj(i -> paras.item(i).getTextContent().trim())
                                .collect(java.util.stream.Collectors.joining(" "));
                        dictionary.fieldDescriptionMap.put(tag, description);
                    });
        } catch (Exception e) {
            LOG.warn("Failed to load phrases file", e);
        }
    }
}