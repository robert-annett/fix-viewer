package com.rannett.fixplugin.ui;

import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class FixDualViewEditorProviderTest extends BasePlatformTestCase {

    @Test
    public void testAcceptAndPolicy() {
        FixDualViewEditorProvider provider = new FixDualViewEditorProvider();
        LightVirtualFile fixFile = new LightVirtualFile("test.fix");
        LightVirtualFile otherFile = new LightVirtualFile("test.txt");

        assertTrue(provider.accept(getProject(), fixFile));
        assertFalse(provider.accept(getProject(), otherFile));
        assertEquals("fix-dual-view", provider.getEditorTypeId());
        assertEquals(FileEditorPolicy.HIDE_DEFAULT_EDITOR, provider.getPolicy());
    }
}
