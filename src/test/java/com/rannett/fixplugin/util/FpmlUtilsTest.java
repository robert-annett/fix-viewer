package com.rannett.fixplugin.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class FpmlUtilsTest {
    @Test
    public void testIsLikelyFpmlTrue() {
        String fpml = "<FpML><trade>test</trade></FpML>";
        assertTrue(FpmlUtils.isLikelyFpml(fpml));
    }

    @Test
    public void testIsLikelyFpmlFalse() {
        String not = "<root><child/></root>";
        assertFalse(FpmlUtils.isLikelyFpml(not));
    }
}
