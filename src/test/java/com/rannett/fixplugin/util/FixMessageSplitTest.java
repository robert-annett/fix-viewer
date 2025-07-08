package com.rannett.fixplugin.util;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FixMessageSplitTest {
    @Test
    public void testSplitMessagesWithEmbeddedXml() {
        String fpml = "<FpML>\n<trade>t</trade>\n</FpML>";
        String msg = "8=FIX.4.4|9=70|35=0|350=" + fpml.length() + "|351=" + fpml + "|10=000|";
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
}
