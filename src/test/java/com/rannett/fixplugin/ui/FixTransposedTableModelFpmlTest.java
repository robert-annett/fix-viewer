package com.rannett.fixplugin.ui;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testParseLongFpmlWithNewlines() {
        String fpml = "<requestConfirmation xmlns=\"http://www.fpml.org/FpML-5/confirmation\" fpmlVersion=\"5-3\">\n  <header>\n    <messageId messageIdScheme=\"http://example.com/msg\">MSG-12345</messageId>\n    <sentBy>ABCDUS33</sentBy>\n    <sendTo>DEFGGB2L</sendTo>\n    <creationTimestamp>2025-07-05T12:00:00Z</creationTimestamp>\n  </header>\n  <trade>\n    <tradeHeader>\n      <partyTradeIdentifier>\n        <partyReference href=\"party1\"/>\n        <tradeId>TRADE-001</tradeId>\n      </partyTradeIdentifier>\n      <tradeDate>2025-07-05</tradeDate>\n    </tradeHeader>\n    <party id=\"party1\">\n      <partyId>ABCDUS33</partyId>\n    </party>\n    <party id=\"party2\">\n      <partyId>DEFGGB2L</partyId>\n    </party>\n    <product>\n      <swap>\n        <swapStream>\n          <payerPartyReference href=\"party1\"/>\n          <receiverPartyReference href=\"party2\"/>\n        </swapStream>\n      </swap>\n    </product>\n  </trade>\n</requestConfirmation>\n";
        String msg = "8=FIX.4.4|9=1003|35=AE|34=2|49=SENDER|56=TARGET|52=20250705-12:30:00.000|55=IR_SWAP|48=SWAP001|22=4|350=" + fpml.length() + "|351=" + fpml + "|10=112|";
        FixTransposedTableModel model = new FixTransposedTableModel(List.of(msg), (id, tag, occ, val) -> {}, null);
        int row = model.getRowForTag("351");
        assertTrue(row >= 0);
        assertEquals(fpml, model.getValueAt(row, 2));
    }

}
