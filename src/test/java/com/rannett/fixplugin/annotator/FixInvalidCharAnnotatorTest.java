package com.rannett.fixplugin.annotator;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import static org.junit.Assert.assertFalse;

public class FixInvalidCharAnnotatorTest extends BasePlatformTestCase {

    public void testFpmlXmlDataNotFlagged() {
        String xml = "<note>\\\ntext</note>";
        String message = "8=FIX.4.4|35=D|213=" + xml + "|10=000|";
        myFixture.configureByText("msg.fix", message);
        var infos = myFixture.doHighlighting();
        assertFalse(infos.stream().anyMatch(i -> {
            String desc = i.getDescription();
            return desc != null && desc.startsWith("Invalid FIX character");
        }));
    }

    public void testFpmlEncodedSecurityDescNotFlagged() {
        String xml = "<note>\\\ntext</note>";
        String message = "8=FIX.4.4|35=D|351=" + xml + "|10=000|";
        myFixture.configureByText("msg.fix", message);
        var infos = myFixture.doHighlighting();
        assertFalse(infos.stream().anyMatch(i -> {
            String desc = i.getDescription();
            return desc != null && desc.startsWith("Invalid FIX character");
        }));
    }
}
