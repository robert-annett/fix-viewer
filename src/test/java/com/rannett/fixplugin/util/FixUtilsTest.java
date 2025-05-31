package com.rannett.fixplugin.util;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FixUtilsTest {

    @Test
    public void testExtractFixVersion_withFIX() {
        String text = "8=FIX.4.2|9=123|35=D|";
        Optional<String> version = FixUtils.extractFixVersion(text);
        assertTrue(version.isPresent());
        assertEquals("FIX.4.2", version.get());
    }

    @Test
    public void testExtractFixVersion_withFIXT() {
        String text = "8=FIXT.1.1|9=456|35=AE|";
        Optional<String> version = FixUtils.extractFixVersion(text);
        assertTrue(version.isPresent());
        assertEquals("FIXT.1.1", version.get());
    }

    @Test
    public void testExtractFixVersion_withSOH() {
        String text = "8=FIX.4.4\u00019=78\u000135=F\u0001";
        Optional<String> version = FixUtils.extractFixVersion(text);
        assertTrue(version.isPresent());
        assertEquals("FIX.4.4", version.get());
    }

    @Test
    public void testExtractFixVersion_noVersion() {
        String text = "35=D|49=ABC|56=XYZ|";
        Optional<String> version = FixUtils.extractFixVersion(text);
        assertFalse(version.isPresent());
    }

    @Test
    public void testExtractFixVersion_atEnd() {
        String text = "35=D|49=ABC|56=XYZ|8=FIX.4.2";
        Optional<String> version = FixUtils.extractFixVersion(text);
        assertTrue(version.isPresent());
        assertEquals("FIX.4.2", version.get());
    }
}
