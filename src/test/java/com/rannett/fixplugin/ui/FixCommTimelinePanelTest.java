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
}
