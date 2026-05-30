package com.rannett.fixplugin.ui;

import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class FixDictionaryFileEditorProviderTest extends BasePlatformTestCase {

    public void testAcceptAndPolicy() {
        FixDictionaryFileEditorProvider provider = new FixDictionaryFileEditorProvider();
        LightVirtualFile dictionaryFile = new LightVirtualFile("FIX.4.4.xml", """
                <fix major="4" minor="4">
                  <messages/>
                  <fields/>
                </fix>
                """);
        LightVirtualFile otherFile = new LightVirtualFile("other.xml", "<root/>");

        assertTrue(provider.accept(getProject(), dictionaryFile));
        assertFalse(provider.accept(getProject(), otherFile));
        assertEquals("fix-dictionary-view", provider.getEditorTypeId());
        assertEquals(FileEditorPolicy.HIDE_DEFAULT_EDITOR, provider.getPolicy());
    }
}
