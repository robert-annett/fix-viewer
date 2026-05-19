package com.rannett.fixplugin.psi.impl;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.rannett.fixplugin.FixElementFactory;
import com.rannett.fixplugin.psi.FixField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FixPsiImplUtilTest extends BasePlatformTestCase {

    // -------------------------------------------------------------------------
    // getTag
    // -------------------------------------------------------------------------

    public void testGetTag_returnsTagNumber() {
        FixField field = FixElementFactory.createFixField(getProject(), "35", "D");
        assertEquals("35", FixPsiImplUtil.getTag(field));
    }

    public void testGetTag_multiDigitTag_returnsFullNumber() {
        FixField field = FixElementFactory.createFixField(getProject(), "1128", "9");
        assertEquals("1128", FixPsiImplUtil.getTag(field));
    }

    public void testGetTag_singleDigitTag_returnsNumber() {
        FixField field = FixElementFactory.createFixField(getProject(), "8", "FIX.4.2");
        assertEquals("8", FixPsiImplUtil.getTag(field));
    }

    /**
     * The source contains replaceAll("\\ ", " ") to convert escaped spaces.
     * For normal numeric tags this is a no-op; verify it does not corrupt standard input.
     */
    public void testGetTag_normalTag_containsNoBackslash() {
        FixField field = FixElementFactory.createFixField(getProject(), "49", "SENDER");
        String tag = FixPsiImplUtil.getTag(field);
        assertEquals("49", tag);
        assertFalse("getTag should not introduce a backslash character", tag.contains("\\"));
    }

    // -------------------------------------------------------------------------
    // getValue
    // -------------------------------------------------------------------------

    public void testGetValue_singleCharValue_returnsValue() {
        FixField field = FixElementFactory.createFixField(getProject(), "35", "D");
        assertEquals("D", FixPsiImplUtil.getValue(field));
    }

    public void testGetValue_numericValue_returnsAsString() {
        FixField field = FixElementFactory.createFixField(getProject(), "34", "42");
        assertEquals("42", FixPsiImplUtil.getValue(field));
    }

    public void testGetValue_multiCharValue_returnsFullText() {
        FixField field = FixElementFactory.createFixField(getProject(), "8", "FIX.4.2");
        assertEquals("FIX.4.2", FixPsiImplUtil.getValue(field));
    }

    public void testGetValue_alphanumericValue_returnsFullText() {
        FixField field = FixElementFactory.createFixField(getProject(), "11", "ORD-001");
        assertEquals("ORD-001", FixPsiImplUtil.getValue(field));
    }

    // -------------------------------------------------------------------------
    // setValue
    // -------------------------------------------------------------------------

    public void testSetValue_changesValueText() {
        FixField field = FixElementFactory.createFixField(getProject(), "35", "D");
        assertEquals("D", FixPsiImplUtil.getValue(field));
        FixPsiImplUtil.setValue(field, "8");
        assertEquals("8", FixPsiImplUtil.getValue(field));
    }

    public void testSetValue_tagRemainsUnchanged() {
        FixField field = FixElementFactory.createFixField(getProject(), "35", "D");
        FixPsiImplUtil.setValue(field, "A");
        assertEquals("35", FixPsiImplUtil.getTag(field));
    }

    public void testSetValue_returnsTheField() {
        FixField field = FixElementFactory.createFixField(getProject(), "35", "D");
        assertNotNull(FixPsiImplUtil.setValue(field, "A"));
    }

    public void testSetValue_multiCharNewValue_updatesCorrectly() {
        FixField field = FixElementFactory.createFixField(getProject(), "49", "OLD");
        FixPsiImplUtil.setValue(field, "NEWSENDER");
        assertEquals("NEWSENDER", FixPsiImplUtil.getValue(field));
    }
}
