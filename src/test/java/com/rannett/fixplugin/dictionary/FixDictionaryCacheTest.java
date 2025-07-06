package com.rannett.fixplugin.dictionary;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.rannett.fixplugin.settings.FixViewerSettingsState;

import java.io.File;
import java.nio.file.Files;

public class FixDictionaryCacheTest extends BasePlatformTestCase {

    public void testCustomPathIsUsed() throws Exception {
        File file = File.createTempFile("dict", ".xml");
        Files.writeString(file.toPath(), "<dictionary>\n<field number=\"1\" name=\"One\" type=\"STRING\"/>\n</dictionary>");

        FixViewerSettingsState settings = FixViewerSettingsState.getInstance(getProject());
        settings.getCustomDictionaryPaths().put("FIX.4.2", file.getAbsolutePath());

        FixDictionaryCache cache = new FixDictionaryCache(getProject());
        FixTagDictionary dict = cache.getDictionary("FIX.4.2");
        assertEquals("One", dict.getTagName("1"));
    }

    public void testDictionaryIsCached() throws Exception {
        File file1 = File.createTempFile("dict1", ".xml");
        Files.writeString(file1.toPath(), "<dictionary>\n<field number=\"1\" name=\"First\" type=\"STRING\"/>\n</dictionary>");
        FixViewerSettingsState settings = FixViewerSettingsState.getInstance(getProject());
        settings.getCustomDictionaryPaths().put("FIX.4.3", file1.getAbsolutePath());

        FixDictionaryCache cache = new FixDictionaryCache(getProject());
        FixTagDictionary first = cache.getDictionary("FIX.4.3");
        assertEquals("First", first.getTagName("1"));

        File file2 = File.createTempFile("dict2", ".xml");
        Files.writeString(file2.toPath(), "<dictionary>\n<field number=\"1\" name=\"Second\" type=\"STRING\"/>\n</dictionary>");
        settings.getCustomDictionaryPaths().put("FIX.4.3", file2.getAbsolutePath());

        FixTagDictionary second = cache.getDictionary("FIX.4.3");
        assertSame(first, second);
        assertEquals("First", second.getTagName("1"));
    }
}
