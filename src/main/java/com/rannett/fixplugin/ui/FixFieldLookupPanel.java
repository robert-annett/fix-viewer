package com.rannett.fixplugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.openapi.util.text.StringUtil;
import com.rannett.fixplugin.dictionary.FieldSection;
import com.rannett.fixplugin.dictionary.FixDictionaryCache;
import com.rannett.fixplugin.dictionary.FixDictionaryChangeListener;
import com.rannett.fixplugin.dictionary.FixTagDictionary;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.util.Map;

/**
 * Panel providing search and display of FIX field information.
 */
public class FixFieldLookupPanel extends JPanel {

    private final JBTextField searchField = new JBTextField();
    private final JBList<String> resultList = new JBList<>(new DefaultListModel<>());
    private final JTextArea detailsArea = new JTextArea();
    private final FixDictionaryCache dictionaryCache;
    private FixTagDictionary dictionary;

    /**
     * Creates a panel for looking up FIX fields.
     *
     * @param project current project context
     */
    public FixFieldLookupPanel(Project project) {
        super(new BorderLayout());
        this.dictionaryCache = project.getService(FixDictionaryCache.class);
        this.dictionary = dictionaryCache.getDictionary("FIX.4.4");
        project.getMessageBus().connect(project).subscribe(FixDictionaryChangeListener.TOPIC, this::reloadDictionary);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        add(searchField, BorderLayout.NORTH);
        add(new JBScrollPane(resultList), BorderLayout.WEST);
        add(new JBScrollPane(detailsArea), BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateResults();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateResults();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateResults();
            }
        });

        resultList.addListSelectionListener(e -> showDetails());
    }

    private void reloadDictionary() {
        dictionaryCache.clear();
        dictionary = dictionaryCache.getDictionary("FIX.4.4");
        SwingUtilities.invokeLater(() -> {
            updateResults();
            showDetails();
        });
    }

    private void updateResults() {
        String term = searchField.getText().trim();
        DefaultListModel<String> model = (DefaultListModel<String>) resultList.getModel();
        model.clear();
        if (term.isEmpty()) {
            return;
        }
        dictionary.getTagNameMap().entrySet().stream()
                .filter(entry -> matches(entry.getKey(), entry.getValue(), term))
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> model.addElement(entry.getKey() + " (" + entry.getValue() + ")"));
    }

    private boolean matches(String tag, String name, String term) {
        if (StringUtil.containsIgnoreCase(tag, term) || StringUtil.containsIgnoreCase(name, term)) {
            return true;
        }
        Map<String, String> values = dictionary.getValueMap(tag);
        return values != null && values.entrySet().stream()
                .anyMatch(e -> StringUtil.containsIgnoreCase(e.getKey(), term)
                        || StringUtil.containsIgnoreCase(e.getValue(), term));
    }

    private void showDetails() {
        String selected = resultList.getSelectedValue();
        if (selected == null) {
            detailsArea.setText("");
            return;
        }
        String tag = selected.split(" ", 2)[0];
        StringBuilder sb = new StringBuilder();
        sb.append("Tag: ").append(tag).append("\n");
        String name = dictionary.getTagName(tag);
        if (name != null) {
            sb.append("Name: ").append(name).append("\n");
        }
        String type = dictionary.getFieldType(tag);
        if (type != null) {
            sb.append("Type: ").append(type).append("\n");
        }
        FieldSection section = dictionary.getFieldSection(tag);
        if (section != null) {
            sb.append("Section: ").append(section.name()).append("\n");
        }
        String description = dictionary.getFieldDescription(tag);
        if (description != null) {
            sb.append("Description: ").append(description).append("\n");
        }
        Map<String, String> values = dictionary.getValueMap(tag);
        if (values != null && !values.isEmpty()) {
            sb.append("Values:\n");
            values.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> sb.append("  ")
                            .append(e.getKey())
                            .append(" = ")
                            .append(e.getValue())
                            .append("\n"));
        }
        detailsArea.setText(sb.toString());
    }
}

