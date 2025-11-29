package com.rannett.fixplugin.ui;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.rannett.fixplugin.settings.FixViewerSettingsState;
import com.rannett.fixplugin.settings.FixViewerSettingsState.DictionaryEntry;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FixTransposedTableModelDictionaryTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FixViewerSettingsState.getInstance(getProject()).setDictionaryEntries(new ArrayList<>());
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            FixViewerSettingsState.getInstance(getProject()).setDictionaryEntries(new ArrayList<>());
        } finally {
            super.tearDown();
        }
    }

    public void testSwitchingDictionariesUpdatesNames() throws Exception {
        File customDictionary = File.createTempFile("custom-dict", ".xml");
        Files.writeString(customDictionary.toPath(), "<dictionary>\n<field number=\"999\" name=\"CustomTag\" type=\"STRING\"/>\n</dictionary>");
        DictionaryEntry customEntry = new DictionaryEntry("FIX.4.2", customDictionary.getAbsolutePath(), false, true);

        List<String> messages = List.of("8=FIX.4.2|35=A|999=value|10=000|");
        FixTransposedTableModel model = new FixTransposedTableModel(messages, (id, tag, occ, value) -> { }, getProject());

        int row = model.getRowForTag("999");
        assertTrue(row >= 0);
        assertEquals("", model.getValueAt(row, 1));

        model.setDictionaryEntry(customEntry);
        assertEquals("CustomTag", model.getValueAt(row, 1));
    }
}
