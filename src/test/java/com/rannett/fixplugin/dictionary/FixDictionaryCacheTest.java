package com.rannett.fixplugin.dictionary;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.rannett.fixplugin.settings.FixViewerSettingsState;
import com.rannett.fixplugin.settings.FixViewerSettingsState.DictionaryEntry;

import java.io.File;
import java.nio.file.Files;

public class FixDictionaryCacheTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FixViewerSettingsState.getInstance(getProject()).setDictionaryEntries(new java.util.ArrayList<>());
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            FixViewerSettingsState.getInstance(getProject()).setDictionaryEntries(new java.util.ArrayList<>());
        } finally {
            super.tearDown();
        }
    }

    public void testCustomPathIsUsed() throws Exception {
        File file = File.createTempFile("dict", ".xml");
        Files.writeString(file.toPath(), "<dictionary>\n<field number=\"1\" name=\"One\" type=\"STRING\"/>\n</dictionary>");

        FixViewerSettingsState settings = FixViewerSettingsState.getInstance(getProject());
        java.util.List<DictionaryEntry> entries = new java.util.ArrayList<>(settings.getDictionaryEntries());
        entries.add(new DictionaryEntry("FIX.4.2", file.getAbsolutePath(), false, true));
        settings.setDictionaryEntries(entries);

        FixDictionaryCache cache = new FixDictionaryCache(getProject());
        FixTagDictionary dict = cache.getDictionary("FIX.4.2");
        assertEquals("One", dict.getTagName("1"));
    }

    public void testDictionaryIsCached() throws Exception {
        File file1 = File.createTempFile("dict1", ".xml");
        Files.writeString(file1.toPath(), "<dictionary>\n<field number=\"1\" name=\"First\" type=\"STRING\"/>\n</dictionary>");
        FixViewerSettingsState settings = FixViewerSettingsState.getInstance(getProject());
        java.util.List<DictionaryEntry> entries = new java.util.ArrayList<>(settings.getDictionaryEntries());
        entries.add(new DictionaryEntry("FIX.4.3", file1.getAbsolutePath(), false, true));
        settings.setDictionaryEntries(entries);

        FixDictionaryCache cache = new FixDictionaryCache(getProject());
        FixTagDictionary first = cache.getDictionary("FIX.4.3");
        assertEquals("First", first.getTagName("1"));

        File file2 = File.createTempFile("dict2", ".xml");
        Files.writeString(file2.toPath(), "<dictionary>\n<field number=\"1\" name=\"Second\" type=\"STRING\"/>\n</dictionary>");
        entries = new java.util.ArrayList<>(settings.getDictionaryEntries());
        entries.removeIf(entry -> "FIX.4.3".equals(entry.getVersion()) && !entry.isBuiltIn());
        entries.add(new DictionaryEntry("FIX.4.3", file2.getAbsolutePath(), false, true));
        settings.setDictionaryEntries(entries);

        FixTagDictionary second = cache.getDictionary("FIX.4.3");
        assertNotSame(first, second);
        assertEquals("Second", second.getTagName("1"));
    }
}
