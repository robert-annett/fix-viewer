package com.rannett.fixplugin.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;

public class FixViewerSettingsConfigurable implements Configurable {

    private JPanel mainPanel;
    private JBTable versionTable;
    private DefaultTableModel tableModel;
    private final FixViewerSettingsState settingsState;

    public FixViewerSettingsConfigurable(Project project) {
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

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(versionTable);
        decorator.setAddAction(button -> tableModel.addRow(new Object[]{"", ""}));
        decorator.setRemoveAction(button -> {
            int selectedRow = versionTable.getSelectedRow();
            if (selectedRow != -1) {
                tableModel.removeRow(selectedRow);
            }
        });
        decorator.setEditAction(button -> {
            int selectedRow = versionTable.getSelectedRow();
            if (selectedRow != -1) {
                String version = (String) tableModel.getValueAt(selectedRow, 0);
                String path = (String) tableModel.getValueAt(selectedRow, 1);

                TextFieldWithBrowseButton chooser = new TextFieldWithBrowseButton();
                FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
                chooser.addBrowseFolderListener("Select Custom Dictionary", "Choose a FIX Dictionary file", null, descriptor);
                chooser.setText(path);

                int result = JOptionPane.showConfirmDialog(mainPanel, chooser, "Edit Custom Path for " + version, JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    tableModel.setValueAt(chooser.getText(), selectedRow, 1);
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
        Map<String, String> current = settingsState.getCustomDictionaryPaths();
        Map<String, String> tableMap = tableToMap();
        return !current.equals(tableMap);
    }

    @Override
    public void apply() throws ConfigurationException {
        settingsState.setCustomDictionaryPaths(tableToMap());
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
