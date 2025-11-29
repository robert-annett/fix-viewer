package com.rannett.fixplugin.ui.dictionary;

import com.intellij.testFramework.LightVirtualFile;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FixDictionaryDetectorTest {

    @Test
    /**
     * Verifies detection succeeds for dictionaries with a FIX root element.
     */
    public void detectsFixDictionaryFromContent() {
        LightVirtualFile file = new LightVirtualFile("FIX.4.4.xml", "<fix><messages></messages></fix>");
        assertTrue(FixDictionaryDetector.isLikelyFixDictionary(file));
    }

    @Test
    /**
     * Ensures non-dictionary XML files are ignored.
     */
    public void rejectsPlainXmlFiles() {
        LightVirtualFile file = new LightVirtualFile("notes.xml", "<notes><note>hi</note></notes>");
        assertFalse(FixDictionaryDetector.isLikelyFixDictionary(file));
    }

    @Test
    /**
     * Confirms the manual override flag always enables the viewer.
     */
    public void respectsForceFlag() {
        LightVirtualFile file = new LightVirtualFile("readme.txt", "irrelevant");
        file.putUserData(FixDictionaryDetector.FORCE_DICTIONARY_VIEWER, true);
        assertTrue(FixDictionaryDetector.isLikelyFixDictionary(file));
    }
}
