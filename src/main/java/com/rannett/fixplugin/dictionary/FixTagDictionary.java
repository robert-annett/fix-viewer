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
import java.util.Map;

public class FixTagDictionary {

    private static final Logger LOG = Logger.getInstance(FixTagDictionary.class);

    private final Map<String, String> tagNameMap = new HashMap<>();
    private final Map<String, Map<String, String>> tagValueMap = new HashMap<>();
    private final Map<String, String> fieldTypeMap = new HashMap<>(); // New map for field types

    FixTagDictionary() {
    }

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

        return dictionary;
    }

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

        return dictionary;
    }

    public String getTagName(String tag) {
        return tagNameMap.get(tag);
    }

    public String getValueName(String tag, String value) {
        Map<String, String> valueMap = tagValueMap.get(tag);
        return valueMap != null ? valueMap.get(value) : null;
    }

    public String getFieldType(String tag) {
        return fieldTypeMap.get(tag); // New method to get field type
    }

    public Map<String, String> getTagNameMap() {
        return Collections.unmodifiableMap(tagNameMap);
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

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("<field")) {
                String tagNumber = extractAttribute(line, "number");
                String tagName = extractAttribute(line, "name");
                String type = extractAttribute(line, "type"); // Extract type attribute
                if (tagNumber != null && tagName != null) {
                    dictionary.tagNameMap.put(tagNumber, tagName);
                    currentTag = tagNumber;
                }
                if (tagNumber != null && type != null) {
                    dictionary.fieldTypeMap.put(tagNumber, type); // Store type
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

    public Map<String, String> getValueMap(String currentTag) {
        return tagValueMap.get(currentTag);
    }
}