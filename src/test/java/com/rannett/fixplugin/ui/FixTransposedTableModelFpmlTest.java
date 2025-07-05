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
}
