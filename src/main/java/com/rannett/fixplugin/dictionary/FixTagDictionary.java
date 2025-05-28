package com.rannett.fixplugin.dictionary;

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

    private final Map<String, String> tagNameMap = new HashMap<>();
    private final Map<String, Map<String, String>> tagValueMap = new HashMap<>();

    private FixTagDictionary() {
    }

    public static FixTagDictionary fromFile(@NotNull File file) throws Exception {
        FixTagDictionary dictionary = new FixTagDictionary();
        String fileName = file.getName().toLowerCase();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            if (fileName.endsWith(".json")) {
                parseJson(reader, dictionary);
            } else if (fileName.endsWith(".xml")) {
                parseXml(reader, dictionary);
            } else {
                throw new IllegalArgumentException("Unsupported file format: " + fileName);
            }
        }

        return dictionary;
    }

    public static FixTagDictionary fromBuiltInVersion(@NotNull String version) {
        FixTagDictionary dictionary = new FixTagDictionary();

        String resourcePath = "/dictionaries/" + version + ".xml";
        try (InputStream inputStream = FixTagDictionary.class.getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Built-in dictionary not found: " + version);
            }
            parseXml(reader, dictionary);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load built-in dictionary: " + version, e);
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

    public Map<String, String> getTagNameMap() {
        return Collections.unmodifiableMap(tagNameMap);
    }

    public Map<String, Map<String, String>> getTagValueMap() {
        return Collections.unmodifiableMap(tagValueMap);
    }

    private static void parseJson(BufferedReader reader, FixTagDictionary dictionary) throws Exception {
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }

        String jsonContent = jsonBuilder.toString();
        // Basic JSON parsing logic (replace with a library like org.json or Jackson for robustness)
        // Example: {"8":"BeginString", "35":{"0":"Heartbeat","1":"Test Request"}}
        org.json.JSONObject root = new org.json.JSONObject(jsonContent);

        for (String key : root.keySet()) {
            Object value = root.get(key);
            if (value instanceof String) {
                dictionary.tagNameMap.put(key, (String) value);
            } else if (value instanceof org.json.JSONObject) {
                org.json.JSONObject valuesObject = (org.json.JSONObject) value;
                Map<String, String> valueMap = new HashMap<>();
                for (String valKey : valuesObject.keySet()) {
                    valueMap.put(valKey, valuesObject.getString(valKey));
                }
                dictionary.tagValueMap.put(key, valueMap);
            }
        }
    }

    private static void parseXml(BufferedReader reader, FixTagDictionary dictionary) throws Exception {
        // Simple XML parsing (replace with a library like JAXB or DOM for robustness)
        // Example format:
        // <dictionary><field number="8" name="BeginString"/><field number="35" name="MsgType"><value enum="0" description="Heartbeat"/></field></dictionary>

        String line;
        String currentTag = null;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("<field")) {
                String tagNumber = extractAttribute(line, "number");
                String tagName = extractAttribute(line, "name");
                if (tagNumber != null && tagName != null) {
                    dictionary.tagNameMap.put(tagNumber, tagName);
                    currentTag = tagNumber;
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
