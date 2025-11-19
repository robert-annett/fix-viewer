package com.rannett.fixplugin.settings;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.rannett.fixplugin.dictionary.FixDictionaryCache;
import com.rannett.fixplugin.dictionary.FixDictionaryChangeListener;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;

public class FixViewerSettingsConfigurable implements Configurable {

    private JPanel mainPanel;
    private JBTable versionTable;
    private DefaultTableModel tableModel;
    private final FixViewerSettingsState settingsState;
    private final Project project;

    public FixViewerSettingsConfigurable(Project project) {
        this.project = project;
        this.settingsState = FixViewerSettingsState.getInstance(project);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "FIX Viewer Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mainPanel = new JPanel();
        String[] columnNames = {"FIX Version", "Custom Dictionary Path"};
        tableModel = new DefaultTableModel(columnNames, 0);
        versionTable = new JBTable(tableModel);

        // Setup file chooser descriptor
        FileChooserDescriptor fileDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withTitle("Select Custom Dictionary")
                .withDescription("Choose an XML or JSON dictionary file.");

        // Decorate the table with add/remove/edit
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(versionTable)
                .setAddAction(button -> tableModel.addRow(new Object[]{"", ""}))
                .setRemoveAction(button -> {
                    int selectedRow = versionTable.getSelectedRow();
                    if (selectedRow != -1) {
                        tableModel.removeRow(selectedRow);
                    }
                })
                .setEditAction(button -> {
                    int selectedRow = versionTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String currentVersion = (String) tableModel.getValueAt(selectedRow, 0);
                        String currentPath = (String) tableModel.getValueAt(selectedRow, 1);
                        DictionaryMappingEditDialog dialog = new DictionaryMappingEditDialog(
                                project,
                                currentVersion,
                                currentPath,
                                fileDescriptor
                        );
                        if (dialog.showAndGet()) {
                            tableModel.setValueAt(dialog.getVersion(), selectedRow, 0);
                            tableModel.setValueAt(dialog.getDictionaryPath(), selectedRow, 1);
                        }
                    }
                });

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(new JLabel("Custom Dictionary Mappings (FIX Version → Path):"));
        mainPanel.add(decorator.createPanel());

        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !settingsState.getCustomDictionaryPaths().equals(tableToMap());
    }

    @Override
    public void apply() {
        settingsState.setCustomDictionaryPaths(tableToMap());
        project.getService(FixDictionaryCache.class).clear();
        project.getMessageBus()
                .syncPublisher(FixDictionaryChangeListener.TOPIC)
                .onDictionariesChanged();
        DaemonCodeAnalyzer.getInstance(project).restart();
    }

    @Override
    public void reset() {
        tableModel.setRowCount(0);
        for (Map.Entry<String, String> entry : settingsState.getCustomDictionaryPaths().entrySet()) {
            tableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }

    @Override
    public void disposeUIResources() {
        // No resources to dispose
    }

    private Map<String, String> tableToMap() {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String version = (String) tableModel.getValueAt(i, 0);
            String path = (String) tableModel.getValueAt(i, 1);
            if (version != null && !version.trim().isEmpty() && path != null && !path.trim().isEmpty()) {
                map.put(version.trim(), path.trim());
            }
        }
        return map;
    }
}
