package com.rannett.fixplugin.dictionary;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class FixTagDictionaryTest {

    @Test
    public void testGetTagNameAndValueName_JSON() {
        String jsonContent = """
                    {
                      "35": {
                        "name": "MsgType",
                        "type": "CHAR",
                        "values": {
                          "0": "Heartbeat",
                          "1": "Test Request"
                        }
                      },
                      "11": {
                        "name": "ClOrdID",
                        "type": "STRING"
                      }
                    }
                """;
        File tempFile = File.createTempFile("test", ".json");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(jsonContent);
        }

        FixTagDictionary dictionary = FixTagDictionary.fromFile(tempFile);
        assertEquals("MsgType", dictionary.getTagName("35"));
        assertEquals("Heartbeat", dictionary.getValueName("35", "0"));
        assertEquals("CHAR", dictionary.getFieldType("35"));
        assertEquals("ClOrdID", dictionary.getTagName("11"));
        assertEquals("STRING", dictionary.getFieldType("11"));
        Assert.assertNull(dictionary.getValueName("11", "X")); // No values defined
    }

    @Test
    public void testGetTagNameAndValueName_XML() {
        String xmlContent = """
                    <dictionary>
                        <field number="35" name="MsgType" type="CHAR">
                            <value enum="0" description="Heartbeat"/>
                            <value enum="1" description="Test Request"/>
                        </field>
                        <field number="11" name="ClOrdID" type="STRING"/>
                    </dictionary>
                """;
        File tempFile = File.createTempFile("test", ".xml");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(xmlContent);
        }

        FixTagDictionary dictionary = FixTagDictionary.fromFile(tempFile);
        assertEquals("MsgType", dictionary.getTagName("35"));
        assertEquals("Heartbeat", dictionary.getValueName("35", "0"));
        assertEquals("CHAR", dictionary.getFieldType("35"));
        assertEquals("ClOrdID", dictionary.getTagName("11"));
        assertEquals("STRING", dictionary.getFieldType("11"));
        assertNull(dictionary.getValueName("11", "X"));
    }

    @Test
    public void testGetFieldTypeUnknownTag() {
        FixTagDictionary dictionary = new FixTagDictionary();
        assertNull(dictionary.getFieldType("9999"));
    }

    @Test
    public void testGetTagNameMapAndValueMap() {
        String jsonContent = """
                    {
                      "35": {
                        "name": "MsgType",
                        "type": "CHAR",
                        "values": {
                          "0": "Heartbeat",
                          "1": "Test Request"
                        }
                      }
                    }
                """;
        File tempFile = File.createTempFile("test", ".json");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(jsonContent);
        }

        FixTagDictionary dictionary = FixTagDictionary.fromFile(tempFile);
        Map<String, String> tagMap = dictionary.getTagNameMap();
        assertTrue(tagMap.containsKey("35"));
        assertEquals(Map.of("0", "Heartbeat", "1", "Test Request"), dictionary.getValueMap("35"));
    }

    @Test
    public void testFromBuiltInVersion_valid() {
        FixTagDictionary dictionary = FixTagDictionary.fromBuiltInVersion("FIX.4.2");
        assertEquals("MsgType", dictionary.getTagName("35"));
        // verify field type lookup from built-in dictionaries
        assertEquals("STRING", dictionary.getFieldType("35"));
    }

    @Test
    public void testFromBuiltInVersion_missingFile() {
        FixTagDictionary dictionary = FixTagDictionary.fromBuiltInVersion("nonexistent-version");
        assertNull(dictionary.getTagName("35"));
    }

    @Test
    public void testFromFile_missingFile() {
        File missing = new File("does-not-exist.json");
        FixTagDictionary dictionary = FixTagDictionary.fromFile(missing);
        assertNull(dictionary.getTagName("35"));
    }

    @Test
    public void testFromFile_unsupportedFormat() throws Exception {
        File tempFile = File.createTempFile("dict", ".txt");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("dummy");
        }
        FixTagDictionary dictionary = FixTagDictionary.fromFile(tempFile);
        assertNull(dictionary.getTagName("35"));
    }

    @Test
    public void testFromFile_corruptJson() throws Exception {
        File tempFile = File.createTempFile("bad", ".json");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("{ bad json");
        }
        FixTagDictionary dictionary = FixTagDictionary.fromFile(tempFile);
        assertNull(dictionary.getTagName("35"));
    }

    @Test
    public void testFromFile_corruptXml() throws Exception {
        File tempFile = File.createTempFile("bad", ".xml");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("<dictionary><field></dictionary>");
        }
        FixTagDictionary dictionary = FixTagDictionary.fromFile(tempFile);
        assertNull(dictionary.getTagName("35"));
    }
}
