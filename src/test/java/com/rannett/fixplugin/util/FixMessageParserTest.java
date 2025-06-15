package com.rannett.fixplugin.util;

import org.junit.Test;
import quickfix.DataDictionary;
import quickfix.Message;

import static org.junit.Assert.*;

public class FixMessageParserTest {

    @Test
    public void testParsePipeDelimitedMessage() throws Exception {
        String msg = "8=FIX.4.2|9=65|35=0|49=AAA|56=BBB|10=000|";
        DataDictionary dd = FixMessageParser.loadDataDictionary("FIX.4.2", null);
        Message m = FixMessageParser.parse(msg, dd);
        assertEquals("FIX.4.2", m.getHeader().getString(8));
        assertEquals("0", m.getHeader().getString(35));
    }

    @Test
    public void testParseSohDelimitedMessage() throws Exception {
        String msg = "8=FIX.4.4\u00019=12\u000135=0\u000110=000\u0001";
        DataDictionary dd = FixMessageParser.loadDataDictionary("FIX.4.4", null);
        Message m = FixMessageParser.parse(msg, dd);
        assertEquals("FIX.4.4", m.getHeader().getString(8));
        assertEquals("0", m.getHeader().getString(35));
    }
}
