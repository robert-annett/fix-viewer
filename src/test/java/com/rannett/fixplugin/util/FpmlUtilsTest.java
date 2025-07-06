package com.rannett.fixplugin.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FpmlUtilsTest {
    @Test
    public void testIsLikelyFpmlTrue() {
        String fpml = "<FpML><trade>test</trade></FpML>";
        assertTrue(FpmlUtils.isLikelyFpml(fpml));
    }

    @Test
    public void testIsLikelyFpmlWithNewlines() {
        String fpml = "<FpML>\n<trade>test</trade>\n</FpML>";
        assertTrue(FpmlUtils.isLikelyFpml(fpml));
    }

    @Test
    public void testIsLikelyFpmlPrefixedRoot() {
        String fpml = "<ir:FpML xmlns:ir='http://example.com/fpml'></ir:FpML>";
        assertTrue(FpmlUtils.isLikelyFpml(fpml));
    }

    @Test
    public void testIsLikelyFpmlFalse() {
        String not = "<root><child/></root>";
        assertFalse(FpmlUtils.isLikelyFpml(not));
    }

    @Test
    public void testIsLikelyFpmlInvalidXml() {
        String invalid = "<FpML>";
        assertFalse(FpmlUtils.isLikelyFpml(invalid));
    }
}
