package com.rannett.fixplugin.ui;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FixTransposedTableModelTest {
    @Test
    public void testGetMessageIdForColumn_bounds() {
        List<String> messages = List.of("8=FIX.4.4|35=A|10=000|");
        FixTransposedTableModel model = new FixTransposedTableModel(messages, (id, tag, occ, value) -> {}, null);

        assertEquals("Message 1", model.getMessageIdForColumn(2));
        assertNull(model.getMessageIdForColumn(1));
        assertNull(model.getMessageIdForColumn(3));
    }
}
