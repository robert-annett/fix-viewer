package com.rannett.fixplugin.dictionary;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;


public class FixTagDictionaryTest {

    @Test
    public void testGetTagNameAndValueName_JSON() throws Exception {
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
    public void testGetTagNameAndValueName_XML() throws Exception {
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
    public void testGetTagNameMapAndValueMap() throws Exception {
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
    public void testFromBuiltInVersion_missingFile() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            FixTagDictionary.fromBuiltInVersion("nonexistent-version");
        });
        String message = exception.getMessage();
        assertTrue(message.contains("Failed to load built-in dictionary"));
    }
}
