package com.rannett.fixplugin.ui;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TagFilterDialogTest extends BasePlatformTestCase {

    public void testInitialSelectionAndDictionaryNames() throws Exception {
        Set<String> tags = Set.of("35", "49");
        Set<String> selected = Set.of("35");
        TagFilterDialog[] holder = new TagFilterDialog[1];
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeAndWait(() -> {
            holder[0] = new TagFilterDialog(getProject(), tags, selected, "FIX.4.2");
        });

        Set<String> result = holder[0].getSelectedTags();
        assertEquals(Set.of("35"), result);

        Field allDisplaysField = TagFilterDialog.class.getDeclaredField("allDisplays");
        allDisplaysField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> displays = (List<String>) allDisplaysField.get(holder[0]);
        assertTrue(displays.stream().anyMatch(d -> d.contains("MsgType")));
    }
}
