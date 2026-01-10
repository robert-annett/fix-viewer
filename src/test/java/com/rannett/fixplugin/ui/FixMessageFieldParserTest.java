package com.rannett.fixplugin.ui;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FixMessageFieldParserTest {

    @Test
    public void testLengthDelimitedFieldPreservesDelimiterCharacters() {
        String message = "8=FIX.4.4|212=5|213=a|b|c|10=000|";
        FixMessageFieldParser parser = new FixMessageFieldParser();

        List<FixMessageFieldParser.TagValue> values = parser.parseMessage(message);

        FixMessageFieldParser.TagValue xmlData = values.stream()
                .filter(tv -> "213".equals(tv.tag()))
                .findFirst()
                .orElseThrow();
        assertEquals("a|b|c", xmlData.value());
    }
}
