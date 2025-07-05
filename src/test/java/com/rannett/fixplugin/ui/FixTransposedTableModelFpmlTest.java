package com.rannett.fixplugin.ui;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FixTransposedTableModelFpmlTest {
    @Test
    public void testParseMessageWithFpml() {
        String fpml = "<FpML>\n<trade>test</trade>\n</FpML>";
        String msg = "8=FIX.4.4|9=70|35=0|350=" + fpml.length() + "|351=" + fpml + "|10=000|";
        FixTransposedTableModel model = new FixTransposedTableModel(List.of(msg), (id, tag, occ, val) -> {}, null);
        int row = model.getRowForTag("351");
        assertTrue(row >= 0);
        assertEquals(fpml, model.getValueAt(row, 2));
    }

    @Test
    public void testParseMessageWithFpmlNoLinebreaks() {
        String fpml = "<FpML><trade>test</trade></FpML>";
        String msg = "8=FIX.4.4|9=64|35=0|350=" + fpml.length() + "|351=" + fpml + "|10=000|";
        FixTransposedTableModel model = new FixTransposedTableModel(List.of(msg), (id, tag, occ, val) -> {}, null);
        int row = model.getRowForTag("351");
        assertTrue(row >= 0);
        assertEquals(fpml, model.getValueAt(row, 2));
    }

    @Test
    public void testParseMessageWithFpmlSohDelimiter() {
        String fpml = "<FpML>\n<trade>t</trade>\n</FpML>";
        String msg = "8=FIX.4.4\u00019=71\u000135=0\u0001350=" + fpml.length() + "\u0001351=" + fpml + "\u000110=000\u0001";
        FixTransposedTableModel model = new FixTransposedTableModel(List.of(msg), (id, tag, occ, val) -> {}, null);
        int row = model.getRowForTag("351");
        assertTrue(row >= 0);
        assertEquals(fpml, model.getValueAt(row, 2));
    }

    @Test
    public void testParseXmlDataWithDelimiterInFpml() {
        String fpml = "<FpML><trade>|special</trade></FpML>";
        String msg = "8=FIX.4.4|9=76|35=0|212=" + fpml.length() + "|213=" + fpml + "|10=000|";
        FixTransposedTableModel model = new FixTransposedTableModel(List.of(msg), (id, tag, occ, val) -> {}, null);
        int row = model.getRowForTag("213");
        assertTrue(row >= 0);
        assertEquals(fpml, model.getValueAt(row, 2));
    }
}
