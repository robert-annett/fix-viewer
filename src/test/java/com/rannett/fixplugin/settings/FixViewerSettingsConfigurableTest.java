package com.rannett.fixplugin.settings;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.rannett.fixplugin.settings.FixViewerSettingsState.DictionaryEntry;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class FixViewerSettingsConfigurableTest extends BasePlatformTestCase {

    public void testSelectingNewDefaultClearsPreviousRowFlag() throws Exception {
        Class<?> modelClass = Class.forName("com.rannett.fixplugin.settings.FixViewerSettingsConfigurable$DictionaryTableModel");
        Constructor<?> constructor = modelClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        AbstractTableModel model = (AbstractTableModel) constructor.newInstance();

        Method setEntries = modelClass.getDeclaredMethod("setEntries", List.class);
        setEntries.setAccessible(true);
        Method getEntry = modelClass.getDeclaredMethod("getEntry", int.class);
        getEntry.setAccessible(true);

        List<DictionaryEntry> rows = new ArrayList<>();
        rows.add(DictionaryEntry.builtIn("FIX.4.2"));
        rows.add(new DictionaryEntry("FIX.4.2", "/tmp/custom.xml", false, false));
        setEntries.invoke(model, rows);

        List<TableModelEvent> events = new ArrayList<>();
        model.addTableModelListener(events::add);

        model.setValueAt(Boolean.TRUE, 1, 2);

        DictionaryEntry clearedDefault = (DictionaryEntry) getEntry.invoke(model, 0);
        DictionaryEntry selectedDefault = (DictionaryEntry) getEntry.invoke(model, 1);

        assertFalse("Previous default row should be cleared", clearedDefault.isDefaultDictionary());
        assertTrue("Newly selected row should be default", selectedDefault.isDefaultDictionary());

        boolean clearedRowUpdated = events.stream()
                .anyMatch(event -> event.getFirstRow() <= 0 && event.getLastRow() >= 0);
        boolean selectedRowUpdated = events.stream()
                .anyMatch(event -> event.getFirstRow() <= 1 && event.getLastRow() >= 1);

        assertTrue("Row losing default should trigger an update", clearedRowUpdated);
        assertTrue("Row gaining default should trigger an update", selectedRowUpdated);
    }
}
