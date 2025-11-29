package com.rannett.fixplugin.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.project.Project;
import com.rannett.fixplugin.dictionary.FixDictionaryCache;
import com.rannett.fixplugin.dictionary.FixTagDictionary;
import com.rannett.fixplugin.settings.FixViewerSettingsState.DictionaryEntry;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.util.*;

public class TagFilterDialog extends DialogWrapper {
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private final JTextField searchField = new JTextField();
    private final Map<String, String> displayToTagMap = new LinkedHashMap<>();
    private final java.util.List<String> allDisplays = new ArrayList<>();

    public TagFilterDialog(@Nullable Project project,
                           Collection<String> tags,
                           Collection<String> initiallySelected,
                           String fixVersion,
                           DictionaryEntry dictionaryEntry) {
        super(project, true);
        FixTagDictionary dict = null;
        if (project != null) {
            dict = project.getService(FixDictionaryCache.class).getDictionary(dictionaryEntry, fixVersion);
        }
        for (String tag : tags) {
            String name = dict != null ? dict.getTagName(tag) : null;
            String display = name != null && !name.isEmpty() ? tag + " (" + name + ")" : tag;
            displayToTagMap.put(display, tag);
            allDisplays.add(display);
            model.addElement(display);
        }
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        if (initiallySelected != null) {
            java.util.List<Integer> indices = new ArrayList<>();
            int i = 0;
            for (String display : allDisplays) {
                if (initiallySelected.contains(displayToTagMap.get(display))) {
                    indices.add(i);
                }
                i++;
            }
            int[] idxArr = indices.stream().mapToInt(Integer::intValue).toArray();
            list.setSelectedIndices(idxArr);
        }

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterList();
            }
        });

        init();
        setTitle("Filter Tags");
    }

    private void filterList() {
        String term = searchField.getText().trim().toLowerCase();
        java.util.List<String> selected = new ArrayList<>(getSelectedTags());
        model.clear();
        java.util.List<Integer> indices = new ArrayList<>();
        int i = 0;
        for (String display : allDisplays) {
            if (term.isEmpty() || display.toLowerCase().contains(term)) {
                model.addElement(display);
                if (selected.contains(displayToTagMap.get(display))) {
                    indices.add(i);
                }
                i++;
            }
        }
        int[] idxArr = indices.stream().mapToInt(Integer::intValue).toArray();
        list.setSelectedIndices(idxArr);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(searchField, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    public Set<String> getSelectedTags() {
        Set<String> sel = new LinkedHashSet<>();
        for (String display : list.getSelectedValuesList()) {
            sel.add(displayToTagMap.get(display));
        }
        return sel;
    }
}
