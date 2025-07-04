package com.rannett.fixplugin.injection;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class FixStringLanguageInjectorTest extends BasePlatformTestCase {

    public void testInjectionOccursForFixString() {
        String code = "class T { String msg = \"8=FIX.4.2|9=5|35=0|10=000|\"; }";
        myFixture.configureByText("T.java", code);
        int offset = myFixture.getFile().getText().indexOf("8=FIX.4.2") + 1;
        assertNotNull(InjectedLanguageManager.getInstance(getProject())
                .findInjectedElementAt(myFixture.getFile(), offset));
    }

    public void testNoInjectionForRegularString() {
        String code = "class T { String msg = \"hello world\"; }";
        myFixture.configureByText("T.java", code);
        int offset = myFixture.getFile().getText().indexOf("hello world") + 1;
        assertNull(InjectedLanguageManager.getInstance(getProject())
                .findInjectedElementAt(myFixture.getFile(), offset));
    }
}

