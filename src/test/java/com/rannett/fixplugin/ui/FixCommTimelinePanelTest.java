package com.rannett.fixplugin.ui;

import org.junit.Test;

import javax.swing.SwingUtilities;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FixCommTimelinePanelTest {
    @Test
    public void testHideHeartbeats() throws Exception {
        List<String> messages = List.of(
                "8=FIX.4.4|35=A|10=001|",
                "8=FIX.4.4|35=0|10=002|"
        );
        SwingUtilities.invokeAndWait(() -> {
            FixCommTimelinePanel panel = new FixCommTimelinePanel(messages);
            assertEquals(2, panel.getVisibleRowCount());
            panel.setHideHeartbeats(true);
            assertEquals(1, panel.getVisibleRowCount());
        });
    }

    @Test
    public void testDirectionDetection() throws Exception {
        List<String> messages = List.of(
                "8=FIXT.1.1|9=65|35=A|34=1|49=BUY_SIDE|56=SELL_SIDE|52=20250829-09:00:00.000|98=0|108=30|141=Y|10=072|",
                "8=FIXT.1.1|9=65|35=A|34=1|49=SELL_SIDE|56=BUY_SIDE|52=20250829-09:00:00.100|98=0|108=30|141=Y|10=082|",
                "8=FIXT.1.1|9=49|35=0|34=2|49=BUY_SIDE|56=SELL_SIDE|52=20250829-09:00:30.000|10=239|",
                "8=FIXT.1.1|9=49|35=0|34=2|49=SELL_SIDE|56=BUY_SIDE|52=20250829-09:00:30.100|10=249|"
        );
        SwingUtilities.invokeAndWait(() -> {
            FixCommTimelinePanel panel = new FixCommTimelinePanel(messages);
            assertEquals("→", panel.getDirectionAtRow(0));
            assertEquals("←", panel.getDirectionAtRow(1));
            assertEquals("→", panel.getDirectionAtRow(2));
            assertEquals("←", panel.getDirectionAtRow(3));
        });
    }
}
