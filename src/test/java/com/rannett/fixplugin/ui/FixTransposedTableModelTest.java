package com.rannett.fixplugin.ui;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

    @Test
    public void testMultipleTagOccurrencesCreateDistinctRows() {
        List<String> messages = List.of("8=FIX.4.4|58=one|58=two|10=000|");
        FixTransposedTableModel model = new FixTransposedTableModel(messages, (id, tag, occ, value) -> { }, null);

        assertEquals(2, model.getRowCount());
        assertEquals("58", model.getValueAt(0, 0));
        assertEquals("one", model.getValueAt(0, 2));
        assertEquals("58", model.getValueAt(1, 0));
        assertEquals("two", model.getValueAt(1, 2));
    }

    @Test
    public void testSetValueAtUsesOccurrence() {
        List<String> messages = List.of("8=FIX.4.4|58=one|58=two|10=000|");
        AtomicReference<String> updated = new AtomicReference<>();
        FixTransposedTableModel model = new FixTransposedTableModel(messages,
                (id, tag, occ, value) -> updated.set(tag + ":" + occ + ":" + value), null);

        model.setValueAt("updated", 1, 2);

        assertEquals("58:2:updated", updated.get());
    }
}
