package com.rannett.fixplugin.dictionary;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FixDictionaryXmlUtilTest {

    @Test
    public void detectsDictionaryShape() {
        String xml = "<fix><fields></fields><messages></messages></fix>";
        assertTrue(FixDictionaryXmlUtil.isFixDictionaryText(xml));
    }

    @Test
    public void detectsSelfClosingDictionarySections() {
        String xml = "<fix><fields/><messages/></fix>";
        assertTrue(FixDictionaryXmlUtil.isFixDictionaryText(xml));
    }

    @Test
    public void rejectsNonDictionaryXml() {
        String xml = "<fix><fields></fields></fix>";
        assertFalse(FixDictionaryXmlUtil.isFixDictionaryText(xml));
    }
}
