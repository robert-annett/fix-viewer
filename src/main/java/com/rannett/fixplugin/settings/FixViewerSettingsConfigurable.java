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
import com.rannett.fixplugin.settings.FixViewerSettingsState.DictionaryEntry;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FixViewerSettingsConfigurable implements Configurable {

    private JPanel mainPanel;
    private JBTable versionTable;
    private DictionaryTableModel tableModel;
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
        tableModel = new DictionaryTableModel();
        versionTable = new JBTable(tableModel);

        // Setup file chooser descriptor
        FileChooserDescriptor fileDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withTitle("Select Custom Dictionary")
                .withDescription("Choose an XML or JSON dictionary file.");

        // Decorate the table with add/remove/edit
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(versionTable)
                .setAddAction(button -> {
                    DictionaryMappingEditDialog dialog = new DictionaryMappingEditDialog(
                            project,
                            "",
                            "",
                            fileDescriptor,
                            true
                    );
                    if (dialog.showAndGet()) {
                        DictionaryEntry entry = new DictionaryEntry(
                                dialog.getVersion(),
                                dialog.getDictionaryPath(),
                                false,
                                dialog.isDefaultDictionary()
                        );
                        tableModel.addEntry(entry);
                    }
                })
                .setRemoveAction(button -> {
                    int selectedRow = versionTable.getSelectedRow();
                    if (selectedRow != -1) {
                        tableModel.removeEntry(selectedRow);
                    }
                })
                .setEditAction(button -> {
                    int selectedRow = versionTable.getSelectedRow();
                    if (selectedRow != -1) {
                        DictionaryEntry entry = tableModel.getEntry(selectedRow);
                        if (entry.isBuiltIn()) {
                            return;
                        }
                        DictionaryMappingEditDialog dialog = new DictionaryMappingEditDialog(
                                project,
                                entry.getVersion(),
                                entry.getPath(),
                                fileDescriptor,
                                entry.isDefaultDictionary()
                        );
                        if (dialog.showAndGet()) {
                            entry.setVersion(dialog.getVersion());
                            entry.setPath(dialog.getDictionaryPath());
                            entry.setDefaultDictionary(dialog.isDefaultDictionary());
                            tableModel.updateEntry(selectedRow, entry);
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
        return !Objects.equals(settingsState.getDictionaryEntries(), tableModel.getEntries());
    }

    @Override
    public void apply() {
        settingsState.setDictionaryEntries(tableModel.getEntries());
        project.getService(FixDictionaryCache.class).clear();
        project.getMessageBus()
                .syncPublisher(FixDictionaryChangeListener.TOPIC)
                .onDictionariesChanged();
        DaemonCodeAnalyzer.getInstance(project).restart();
    }

    @Override
    public void reset() {
        tableModel.setEntries(settingsState.getDictionaryEntries());
    }

    @Override
    public void disposeUIResources() {
        // No resources to dispose
    }

    private static final class DictionaryTableModel extends AbstractTableModel {
        private final String[] columnNames = {"FIX Version", "Dictionary Path", "Default"};
        private final List<DictionaryEntry> entries = new ArrayList<>();

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 2) {
                return Boolean.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            DictionaryEntry entry = entries.get(rowIndex);
            return !entry.isBuiltIn();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DictionaryEntry entry = entries.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> entry.getVersion();
                case 1 -> entry.isBuiltIn() ? "Bundled dictionary" : entry.getPath();
                case 2 -> entry.isDefaultDictionary();
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            DictionaryEntry entry = entries.get(rowIndex);
            if (entry.isBuiltIn()) {
                return;
            }
            if (columnIndex == 0) {
                entry.setVersion(aValue != null ? aValue.toString() : "");
            } else if (columnIndex == 1) {
                entry.setPath(aValue != null ? aValue.toString() : "");
            } else if (columnIndex == 2) {
                boolean selected = aValue instanceof Boolean && (Boolean) aValue;
                entry.setDefaultDictionary(selected);
                if (selected) {
                    clearDefaultsForVersion(entry.getVersion(), rowIndex);
                }
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
        }

        void addEntry(DictionaryEntry entry) {
            if (entry.isDefaultDictionary()) {
                clearDefaultsForVersion(entry.getVersion(), -1);
            }
            entries.add(entry);
            fireTableDataChanged();
        }

        void removeEntry(int index) {
            if (index < 0 || index >= entries.size()) {
                return;
            }
            if (entries.get(index).isBuiltIn()) {
                return;
            }
            entries.remove(index);
            fireTableDataChanged();
        }

        void updateEntry(int index, DictionaryEntry entry) {
            if (entry.isDefaultDictionary()) {
                clearDefaultsForVersion(entry.getVersion(), index);
            }
            entries.set(index, entry);
            fireTableRowsUpdated(index, index);
        }

        DictionaryEntry getEntry(int index) {
            return entries.get(index);
        }

        List<DictionaryEntry> getEntries() {
            return new ArrayList<>(entries);
        }

        void setEntries(List<DictionaryEntry> newEntries) {
            entries.clear();
            entries.addAll(newEntries);
            fireTableDataChanged();
        }

        private void clearDefaultsForVersion(String version, int skipIndex) {
            for (int i = 0; i < entries.size(); i++) {
                if (i == skipIndex) {
                    continue;
                }
                DictionaryEntry entry = entries.get(i);
                if (Objects.equals(entry.getVersion(), version)) {
                    entry.setDefaultDictionary(false);
                }
            }
        }
    }
}
