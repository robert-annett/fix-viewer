package com.rannett.fixplugin.settings;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class FixViewerSettingsStateTest {

    private FixViewerSettingsState state;

    @Before
    public void setUp() {
        state = new FixViewerSettingsState();
    }

    // -------------------------------------------------------------------------
    // ensureBuiltInDictionariesPresent
    // -------------------------------------------------------------------------

    @Test
    public void getDictionaryEntries_addsAllThreeBuiltInsWhenEmpty() {
        List<FixViewerSettingsState.DictionaryEntry> entries = state.getDictionaryEntries();
        assertEquals(3, entries.size());
        assertTrue(entries.stream().anyMatch(e -> e.getVersion().equals("FIX.4.2") && e.isBuiltIn()));
        assertTrue(entries.stream().anyMatch(e -> e.getVersion().equals("FIX.4.4") && e.isBuiltIn()));
        assertTrue(entries.stream().anyMatch(e -> e.getVersion().equals("FIXT.1.1") && e.isBuiltIn()));
    }

    @Test
    public void getDictionaryEntries_doesNotDuplicateBuiltInsOnRepeatedCalls() {
        state.getDictionaryEntries();
        state.getDictionaryEntries();
        long count = state.getDictionaryEntries().stream()
                .filter(e -> e.getVersion().equals("FIX.4.2") && e.isBuiltIn())
                .count();
        assertEquals(1, count);
    }

    @Test
    public void setDictionaryEntries_preservesCustomEntryAndAddsBuiltIns() {
        List<FixViewerSettingsState.DictionaryEntry> custom = new ArrayList<>();
        custom.add(new FixViewerSettingsState.DictionaryEntry("FIX.4.2", "/custom.xml", false, true));
        state.setDictionaryEntries(custom);

        List<FixViewerSettingsState.DictionaryEntry> entries = state.getDictionaryEntries();
        assertTrue(entries.stream().anyMatch(e -> "/custom.xml".equals(e.getPath())));
        assertTrue(entries.stream().anyMatch(e -> e.getVersion().equals("FIX.4.4") && e.isBuiltIn()));
        assertTrue(entries.stream().anyMatch(e -> e.getVersion().equals("FIXT.1.1") && e.isBuiltIn()));
    }

    // -------------------------------------------------------------------------
    // getDefaultDictionary — fallback chain
    // -------------------------------------------------------------------------

    @Test
    public void getDefaultDictionary_withOnlyBuiltIn_returnsBuiltIn() {
        FixViewerSettingsState.DictionaryEntry result = state.getDefaultDictionary("FIX.4.2");
        assertNotNull(result);
        assertTrue(result.isBuiltIn());
        assertEquals("FIX.4.2", result.getVersion());
    }

    @Test
    public void getDefaultDictionary_unknownVersion_returnsNull() {
        assertNull(state.getDefaultDictionary("FIX.9.9"));
    }

    @Test
    public void getDefaultDictionary_withCustomMarkedDefault_prefersCustomOverBuiltIn() {
        List<FixViewerSettingsState.DictionaryEntry> entries = new ArrayList<>();
        entries.add(new FixViewerSettingsState.DictionaryEntry("FIX.4.2", "/custom.xml", false, true));
        state.setDictionaryEntries(entries);

        FixViewerSettingsState.DictionaryEntry result = state.getDefaultDictionary("FIX.4.2");
        assertNotNull(result);
        assertFalse(result.isBuiltIn());
        assertEquals("/custom.xml", result.getPath());
    }

    @Test
    public void getDefaultDictionary_withCustomNotMarkedDefault_returnsBuiltIn() {
        // ensureBuiltInDictionariesPresent always adds built-ins with defaultDictionary=true, so the
        // built-in wins at tier 2 (any isDefaultDictionary()) when the custom has defaultDictionary=false.
        List<FixViewerSettingsState.DictionaryEntry> entries = new ArrayList<>();
        entries.add(new FixViewerSettingsState.DictionaryEntry("FIX.4.2", "/custom.xml", false, false));
        state.setDictionaryEntries(entries);

        FixViewerSettingsState.DictionaryEntry result = state.getDefaultDictionary("FIX.4.2");
        assertNotNull(result);
        assertTrue(result.isBuiltIn());
    }

    @Test
    public void getDefaultDictionary_withMultipleCustom_prefersExplicitlyMarkedDefault() {
        List<FixViewerSettingsState.DictionaryEntry> entries = new ArrayList<>();
        entries.add(new FixViewerSettingsState.DictionaryEntry("FIX.4.2", "/first.xml", false, false));
        entries.add(new FixViewerSettingsState.DictionaryEntry("FIX.4.2", "/second.xml", false, true));
        state.setDictionaryEntries(entries);

        FixViewerSettingsState.DictionaryEntry result = state.getDefaultDictionary("FIX.4.2");
        assertNotNull(result);
        assertEquals("/second.xml", result.getPath());
    }

    // -------------------------------------------------------------------------
    // normalizeDefaults — exactly one default per version after setDictionaryEntries
    // -------------------------------------------------------------------------

    @Test
    public void setDictionaryEntries_withTwoCustomsBothDefault_normalizesToOneDefault() {
        List<FixViewerSettingsState.DictionaryEntry> entries = new ArrayList<>();
        entries.add(new FixViewerSettingsState.DictionaryEntry("FIX.4.2", "/first.xml", false, true));
        entries.add(new FixViewerSettingsState.DictionaryEntry("FIX.4.2", "/second.xml", false, true));
        state.setDictionaryEntries(entries);

        long defaultCount = state.getDictionariesForVersion("FIX.4.2").stream()
                .filter(FixViewerSettingsState.DictionaryEntry::isDefaultDictionary)
                .count();
        assertEquals(1, defaultCount);
    }

    @Test
    public void setDictionaryEntries_withNoCustomDefault_builtInBecomesDefault() {
        List<FixViewerSettingsState.DictionaryEntry> entries = new ArrayList<>();
        state.setDictionaryEntries(entries);

        FixViewerSettingsState.DictionaryEntry def = state.getDefaultDictionary("FIX.4.2");
        assertNotNull(def);
        assertTrue(def.isBuiltIn());
    }

    // -------------------------------------------------------------------------
    // getDictionariesForVersion
    // -------------------------------------------------------------------------

    @Test
    public void getDictionariesForVersion_returnsOnlyEntriesForThatVersion() {
        List<FixViewerSettingsState.DictionaryEntry> result = state.getDictionariesForVersion("FIX.4.2");
        assertTrue(result.stream().allMatch(e -> e.getVersion().equals("FIX.4.2")));
        assertFalse(result.isEmpty());
    }

    @Test
    public void getDictionariesForVersion_unknownVersion_returnsEmptyList() {
        assertTrue(state.getDictionariesForVersion("FIX.9.9").isEmpty());
    }

    // -------------------------------------------------------------------------
    // DictionaryEntry.getCacheKey
    // -------------------------------------------------------------------------

    @Test
    public void getCacheKey_builtIn_prefixedWithBUILTIN() {
        FixViewerSettingsState.DictionaryEntry entry = FixViewerSettingsState.DictionaryEntry.builtIn("FIX.4.2");
        assertEquals("BUILTIN:FIX.4.2", entry.getCacheKey());
    }

    @Test
    public void getCacheKey_custom_prefixedWithCUSTOM() {
        FixViewerSettingsState.DictionaryEntry entry =
                new FixViewerSettingsState.DictionaryEntry("FIX.4.2", "/my/dict.xml", false, false);
        assertEquals("CUSTOM:/my/dict.xml", entry.getCacheKey());
    }

    // -------------------------------------------------------------------------
    // DictionaryEntry.toString
    // -------------------------------------------------------------------------

    @Test
    public void toString_builtIn_includesBundledLabel() {
        FixViewerSettingsState.DictionaryEntry entry = FixViewerSettingsState.DictionaryEntry.builtIn("FIX.4.2");
        String str = entry.toString();
        assertTrue(str.contains("FIX.4.2"));
        assertTrue(str.contains("Built-in"));
        assertTrue(str.contains("Bundled"));
    }

    @Test
    public void toString_custom_includesPath() {
        FixViewerSettingsState.DictionaryEntry entry =
                new FixViewerSettingsState.DictionaryEntry("FIX.4.2", "/my/dict.xml", false, false);
        String str = entry.toString();
        assertTrue(str.contains("FIX.4.2"));
        assertTrue(str.contains("Custom"));
        assertTrue(str.contains("/my/dict.xml"));
    }
}
