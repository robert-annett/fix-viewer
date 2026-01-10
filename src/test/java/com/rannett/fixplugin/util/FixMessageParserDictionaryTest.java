package com.rannett.fixplugin.util;

import com.rannett.fixplugin.settings.FixViewerSettingsState.DictionaryEntry;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertNotNull;

public class FixMessageParserDictionaryTest {

    @Test
    public void testLoadCustomDictionaryPath() throws IOException {
        Path tempFile = Files.createTempFile("fix-dict", ".xml");
        tempFile.toFile().deleteOnExit();

        try (InputStream stream = FixMessageParser.class.getResourceAsStream("/dictionaries/FIX.4.4.xml")) {
            assertNotNull(stream);
            Files.copy(stream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        DictionaryEntry entry = new DictionaryEntry("FIX.4.4", tempFile.toString(), false, true);

        assertNotNull(FixMessageParser.loadDataDictionary("FIX.4.4", entry));
    }

    @Test
    public void testLoadDictionaryFallsBackWhenCustomPathFails() {
        DictionaryEntry entry = new DictionaryEntry("FIX.4.4", "missing-dictionary.xml", false, true);

        assertNotNull(FixMessageParser.loadDataDictionary("FIX.4.4", entry));
    }
}
