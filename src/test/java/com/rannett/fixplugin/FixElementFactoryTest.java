package com.rannett.fixplugin;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.rannett.fixplugin.psi.FixField;

import static org.junit.Assert.assertEquals;

public class FixElementFactoryTest extends BasePlatformTestCase {

    public void testCreateFixFieldAndSetValue() {
        FixField field = FixElementFactory.createFixField(getProject(), "35", "A");
        assertEquals("35", field.getTag());
        assertEquals("A", field.getValue());

        field.setValue("B");
        assertEquals("B", field.getValue());
    }
}
