package com.rannett.fixplugin.ui;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import javax.swing.JTextArea;
import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link FixFieldLookupPanel}.
 */
public class FixFieldLookupPanelTest extends BasePlatformTestCase {

    /**
     * Ensures that the details text area wraps long lines so horizontal scrolling is unnecessary.
     */
    public void testDescriptionWraps() {
        JTextArea area;
        try {
            FixFieldLookupPanel panel = new FixFieldLookupPanel(getProject());
            Field field = FixFieldLookupPanel.class.getDeclaredField("detailsArea");
            field.setAccessible(true);
            area = (JTextArea) field.get(panel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertTrue(area.getLineWrap());
        assertTrue(area.getWrapStyleWord());
    }
}
