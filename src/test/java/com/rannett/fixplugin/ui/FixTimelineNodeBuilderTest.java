package com.rannett.fixplugin.ui;

import org.junit.Test;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FixTimelineNodeBuilderTest {

    @Test
    public void testParseErrorNodeAddsErrorChild() {
        FixTimelineNodeBuilder builder = new FixTimelineNodeBuilder();

        FixTimelineMessageNode node = builder.buildNode("not-a-fix-message", 1);

        assertEquals("not-a-fix-message", node.getUserObject());
        assertEquals(1, node.getChildCount());
        DefaultMutableTreeNode errorNode = (DefaultMutableTreeNode) node.getChildAt(0);
        assertTrue(errorNode.getUserObject().toString().startsWith("Parse error:"));
    }

    @Test
    public void testGroupFieldsAppearUnderBodyNode() {
        FixTimelineNodeBuilder builder = new FixTimelineNodeBuilder();
        String message = "8=FIX.4.4|9=75|35=D|49=SENDER|56=TARGET|34=1|52=20240101-00:00:00.000|" +
                "453=1|448=PARTY|447=D|452=1|10=000|";

        FixTimelineMessageNode node = builder.buildNode(message, 1);

        DefaultMutableTreeNode bodyNode = (DefaultMutableTreeNode) node.getChildAt(1);
        boolean foundGroup = hasChildWithPrefix(bodyNode, "NoPartyIDs [1]");
        assertTrue(foundGroup);
    }

    private static boolean hasChildWithPrefix(DefaultMutableTreeNode parent, String prefix) {
        Enumeration<?> children = parent.children();
        while (children.hasMoreElements()) {
            Object child = children.nextElement();
            if (child instanceof DefaultMutableTreeNode) {
                String label = ((DefaultMutableTreeNode) child).getUserObject().toString();
                if (label.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }
}
