package com.rannett.fixplugin.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;

public class FixViewerSettingsConfigurable implements Configurable {

    private JPanel mainPanel;
    private JBTable versionTable;
    private DefaultTableModel tableModel;
    private JBTable colorTable;
    private DefaultTableModel colorModel;
    private JTextField headerFieldsField;
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

        String[] colorCols = {"Field Type", "Color (#RRGGBB)"};
        colorModel = new DefaultTableModel(colorCols, 0);
        colorTable = new JBTable(colorModel);

        // Setup file chooser descriptor
        FileChooserDescriptor fileDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withTitle("Select Custom Dictionary")
                .withDescription("Choose an XML or JSON dictionary file.");

        // Decorate the version table with add/remove/edit
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

                        JPanel panel = new JPanel();
                        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                        JTextField versionField = new JTextField(currentVersion);
                        TextFieldWithBrowseButton fileField = new TextFieldWithBrowseButton();
                        fileField.setText(currentPath);
                        fileField.addBrowseFolderListener(new TextBrowseFolderListener(fileDescriptor, project));


                        panel.add(new JLabel("FIX Version:"));
                        panel.add(versionField);
                        panel.add(new JLabel("Custom Dictionary Path:"));
                        panel.add(fileField);

                        int result = JOptionPane.showConfirmDialog(mainPanel, panel, "Edit Mapping", JOptionPane.OK_CANCEL_OPTION);
                        if (result == JOptionPane.OK_OPTION) {
                            String newVersion = versionField.getText().trim();
                            String newPath = fileField.getText().trim();
                            tableModel.setValueAt(newVersion, selectedRow, 0);
                            tableModel.setValueAt(newPath, selectedRow, 1);
                        }
                    }
                });

        ToolbarDecorator colorDecorator = ToolbarDecorator.createDecorator(colorTable)
                .setAddAction(button -> colorModel.addRow(new Object[]{"", "#000000"}))
                .setRemoveAction(button -> {
                    int row = colorTable.getSelectedRow();
                    if (row != -1) {
                        colorModel.removeRow(row);
                    }
                });

        headerFieldsField = new JTextField();

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(new JLabel("Custom Dictionary Mappings (FIX Version → Path):"));
        mainPanel.add(decorator.createPanel());
        mainPanel.add(new JLabel("Field Type Colours:"));
        mainPanel.add(colorDecorator.createPanel());
        mainPanel.add(new JLabel("Header Fields (comma separated tags):"));
        mainPanel.add(headerFieldsField);

        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !settingsState.getCustomDictionaryPaths().equals(tableToMap()) ||
                !settingsState.getFieldTypeColors().equals(colorTableToMap()) ||
                !headerFieldsField.getText().trim().equals(settingsState.getHeaderLabelFields());
    }

    @Override
    public void apply() {
        settingsState.setCustomDictionaryPaths(tableToMap());
        settingsState.setFieldTypeColors(colorTableToMap());
        settingsState.setHeaderLabelFields(headerFieldsField.getText().trim());
    }

    @Override
    public void reset() {
        tableModel.setRowCount(0);
        for (Map.Entry<String, String> entry : settingsState.getCustomDictionaryPaths().entrySet()) {
            tableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
        colorModel.setRowCount(0);
        for (Map.Entry<String, String> entry : settingsState.getFieldTypeColors().entrySet()) {
            colorModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
        headerFieldsField.setText(settingsState.getHeaderLabelFields());
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

    private Map<String, String> colorTableToMap() {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < colorModel.getRowCount(); i++) {
            String type = (String) colorModel.getValueAt(i, 0);
            String color = (String) colorModel.getValueAt(i, 1);
            if (type != null && !type.trim().isEmpty() && color != null && !color.trim().isEmpty()) {
                map.put(type.trim(), color.trim());
            }
        }
        return map;
    }
}
