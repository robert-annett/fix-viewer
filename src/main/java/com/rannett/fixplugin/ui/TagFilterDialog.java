package com.rannett.fixplugin.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.project.Project;
import com.rannett.fixplugin.dictionary.FixDictionaryCache;
import com.rannett.fixplugin.dictionary.FixTagDictionary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

public class TagFilterDialog extends DialogWrapper {
    private final JList<String> list;
    private final Map<String, String> displayToTagMap = new LinkedHashMap<>();

    public TagFilterDialog(@Nullable Project project,
                           Collection<String> tags,
                           Collection<String> initiallySelected,
                           String fixVersion) {
        super(project, true);
        FixTagDictionary dict = null;
        if (project != null) {
            dict = project.getService(FixDictionaryCache.class).getDictionary(fixVersion);
        }
        for (String tag : tags) {
            String name = dict != null ? dict.getTagName(tag) : null;
            String display = name != null && !name.isEmpty() ? tag + " (" + name + ")" : tag;
            displayToTagMap.put(display, tag);
        }
        list = new JList<>(displayToTagMap.keySet().toArray(new String[0]));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        if (initiallySelected != null) {
            java.util.List<Integer> indices = new ArrayList<>();
            int i = 0;
            for (String display : displayToTagMap.keySet()) {
                if (initiallySelected.contains(displayToTagMap.get(display))) {
                    indices.add(i);
                }
                i++;
            }
            int[] idxArr = indices.stream().mapToInt(Integer::intValue).toArray();
            list.setSelectedIndices(idxArr);
        }

        init();
        setTitle("Filter Tags");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return new JScrollPane(list);
    }

    public Set<String> getSelectedTags() {
        Set<String> sel = new LinkedHashSet<>();
        for (String display : list.getSelectedValuesList()) {
            sel.add(displayToTagMap.get(display));
        }
        return sel;
    }
}
