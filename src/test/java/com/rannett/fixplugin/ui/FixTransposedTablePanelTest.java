package com.rannett.fixplugin.ui;

import org.junit.Test;

import java.util.List;
import javax.swing.JTable;

import static org.junit.Assert.*;

public class FixTransposedTablePanelTest {
    @Test
    public void testGetMessageText() {
        List<String> messages = List.of("8=FIX.4.4|35=A|10=111|", "8=FIX.4.4|35=B|10=222|");
        FixTransposedTablePanel panel = new FixTransposedTablePanel(messages, (id, tag, occ, val) -> {}, null);
        assertEquals("8=FIX.4.4|35=A|10=111|", panel.getMessageText("Message 1"));
        assertEquals("8=FIX.4.4|35=B|10=222|", panel.getMessageText("Message 2"));
        assertEquals("", panel.getMessageText("Message 3"));
    }

    @Test
    public void testAutoResizeOff() throws Exception {
        List<String> messages = List.of("8=FIX.4.4|35=A|10=111|", "8=FIX.4.4|35=B|10=222|");
        final JTable[] tableHolder = new JTable[1];
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            FixTransposedTablePanel panel = new FixTransposedTablePanel(messages, (id, tag, occ, val) -> {}, null);
            try {
                java.lang.reflect.Field tableField = FixTransposedTablePanel.class.getDeclaredField("table");
                tableField.setAccessible(true);
                tableHolder[0] = (JTable) tableField.get(panel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals(JTable.AUTO_RESIZE_OFF, tableHolder[0].getAutoResizeMode());
    }
}
