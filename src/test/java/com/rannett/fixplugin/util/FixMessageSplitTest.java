package com.rannett.fixplugin.util;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FixMessageSplitTest {

    private static final char SOH = '\u0001';

    @Test
    public void testSplitMessagesWithEmbeddedFpmlPipeDelimiter() {
        String fpml = sampleFpmlSnippet();
        String msg = "8=FIX.4.4|9=228|35=j|148=TradeWithFpml|" +
                "58=Contains embedded FpML payload|350=" + fpml.length() + "|351=" + fpml + "|10=000|";
        String combined = msg + "\n" + msg;
        List<String> parsed = FixMessageParser.splitMessages(combined);
        assertEquals(2, parsed.size());
        assertEquals(msg, parsed.get(0));
        assertEquals(msg, parsed.get(1));
    }

    @Test
    public void testSplitMessagesIgnoresComments() {
        String msg = "8=FIX.4.4|9=12|35=0|10=000|";
        String text = "#first\n" + msg + "\n#second\n" + msg;
        List<String> parsed = FixMessageParser.splitMessages(text);
        assertEquals(4, parsed.size());
        assertEquals("#first", parsed.get(0));
        assertEquals(msg, parsed.get(1));
        assertEquals("#second", parsed.get(2));
        assertEquals(msg, parsed.get(3));
    }

    @Test
    public void testSplitMessagesWindowsNewlines() {
        String msg = "8=FIX.4.2|9=12|35=0|10=000|";
        String text = msg + "\r\n" + msg + "\r\n";
        List<String> parsed = FixMessageParser.splitMessages(text);
        assertEquals(2, parsed.size());
        assertEquals(msg, parsed.get(0));
        assertEquals(msg, parsed.get(1));
    }

    @Test
    public void testSplitMessagesHandlesLeadingText() {
        String msg = "8=FIX.4.2|9=12|35=0|10=000|";
        String text = "note\n" + msg;
        List<String> parsed = FixMessageParser.splitMessages(text);
        assertEquals(2, parsed.size());
        assertEquals("note", parsed.get(0));
        assertEquals(msg, parsed.get(1));
    }

    @Test
    public void testSplitMessagesSkipsBlankLinesOnlyInput() {
        String text = "\n\r\n  \n";
        List<String> parsed = FixMessageParser.splitMessages(text);
        assertEquals(0, parsed.size());
    }

    @Test
    public void testSplitMessagesIgnoresUndelimitedChecksumToken() {
        String message = "8=FIX.4.4|9=44|35=0|58=note10=abc|10=000|";
        List<String> parsed = FixMessageParser.splitMessages(message);
        assertEquals(1, parsed.size());
        assertEquals(message, parsed.get(0));
    }

    @Test
    public void testSplitMessagesSkipsWhitespaceBetweenMessages() {
        String msg = "8=FIX.4.2|9=12|35=0|10=000|";
        String text = msg + "\n\n   \n" + msg;
        List<String> parsed = FixMessageParser.splitMessages(text);
        assertEquals(2, parsed.size());
        assertEquals(msg, parsed.get(0));
        assertEquals(msg, parsed.get(1));
    }

    @Test
    public void testSplitMessagesTradeCaptureReportUnderlyingPx() {
        String tradeCaptureReport = "8=FIX.4.4|9=185|35=AE|34=2|49=SENDER|52=20230801-09:30:00.000|56=TARGET|" +
                "571=TRD-12345|487=0|17=EXEC-555|150=2|39=2|55=XYZ|31=110.5|32=100|" +
                "110=10.5|711=1|311=UNDERLY|309=ISIN1234567|305=4|652=110.5|10=220|";
        List<String> parsed = FixMessageParser.splitMessages(tradeCaptureReport);
        assertEquals(1, parsed.size());
        assertEquals(tradeCaptureReport, parsed.get(0));
    }

    @Test
    public void testSplitMessagesTradeCaptureReportUnderlyingPxSohDelimiter() {
        String tradeCaptureReport = "8=FIX.4.4" + SOH +
                "9=185" + SOH +
                "35=AE" + SOH +
                "34=2" + SOH +
                "49=SENDER" + SOH +
                "52=20230801-09:30:00.000" + SOH +
                "56=TARGET" + SOH +
                "571=TRD-12345" + SOH +
                "487=0" + SOH +
                "17=EXEC-555" + SOH +
                "150=2" + SOH +
                "39=2" + SOH +
                "55=XYZ" + SOH +
                "31=110.5" + SOH +
                "32=100" + SOH +
                "110=10.5" + SOH +
                "711=1" + SOH +
                "311=UNDERLY" + SOH +
                "309=ISIN1234567" + SOH +
                "305=4" + SOH +
                "652=110.5" + SOH +
                "10=220" + SOH;
        List<String> parsed = FixMessageParser.splitMessages(tradeCaptureReport);
        assertEquals(1, parsed.size());
        assertEquals(tradeCaptureReport, parsed.get(0));
    }

    @Test
    public void testSplitMessagesWithEmbeddedFpmlSohDelimiter() {
        String fpml = sampleFpmlSnippet();
        String message = "8=FIX.4.4" + SOH +
                "9=228" + SOH +
                "35=j" + SOH +
                "148=TradeWithFpml" + SOH +
                "58=Contains embedded FpML payload" + SOH +
                "350=" + fpml.length() + SOH +
                "351=" + fpml + SOH +
                "10=000" + SOH;
        String combined = message + "\n" + message;
        List<String> parsed = FixMessageParser.splitMessages(combined);
        assertEquals(2, parsed.size());
        assertEquals(message, parsed.get(0));
        assertEquals(message, parsed.get(1));
    }

    private static String sampleFpmlSnippet() {
        return "<fpml:trade xmlns:fpml=\"http://www.fpml.org/FpML-5/confirmation\">\n" +
                "  <fpml:tradeHeader>\n" +
                "    <fpml:partyReference href=\"Party1\"/>\n" +
                "    <fpml:partyReference href=\"Party2\"/>\n" +
                "  </fpml:tradeHeader>\n" +
                "</fpml:trade>";
    }
}
