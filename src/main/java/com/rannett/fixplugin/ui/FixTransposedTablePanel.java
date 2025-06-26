package com.rannett.fixplugin.ui;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.table.JBTable;
import com.rannett.fixplugin.FixFileType;
import com.rannett.fixplugin.dictionary.FixDictionaryCache;
import com.rannett.fixplugin.dictionary.FixTagDictionary;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FixTransposedTablePanel extends JPanel {
    private final FixTransposedTableModel model;
    private final JBTable table;
    private Runnable onCellSelectedCallback;
    private final List<TableColumn> allColumns = new ArrayList<>();
    private final Project project;
    private List<String> messages;

    public FixTransposedTablePanel(List<String> fixMessages, FixTransposedTableModel.DocumentUpdater updater, Project project) {
        super(new BorderLayout());
        this.project = project;
        this.messages = new ArrayList<>(fixMessages);
        model = new FixTransposedTableModel(fixMessages, updater, project);
        table = new JBTable(model);
        table.setFillsViewportHeight(true);

        // Custom renderer to show value + description
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                String displayValue = String.valueOf(value);
                if (column >= 2) {
                    String tag = model.getTagAtRow(row);
                    String valStr = String.valueOf(value);
                    FixTagDictionary dictionary = project.getService(FixDictionaryCache.class).getDictionary(model.getFixVersion());
                    String desc = dictionary.getValueName(tag, valStr);
                    if (desc != null && !desc.isEmpty()) {
                        displayValue = valStr + " (" + desc + ")";
                    }
                }
                setText(displayValue);
                return c;
            }
        });

        // Custom editor with ComboBox showing value (desc) but committing only value
        table.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()) {
            private ComboBox<String> comboBox;
            private Map<String, String> displayToValueMap;

            @Override
            public Component getTableCellEditorComponent(JTable tbl, Object value, boolean isSelected, int row, int column) {
                String currentTag = model.getTagAtRow(row);
                FixTagDictionary dictionary = project.getService(FixDictionaryCache.class).getDictionary(model.getFixVersion());
                Map<String, String> enums = dictionary.getValueMap(currentTag);

                if (column >= 2 && enums != null && !enums.isEmpty()) {
                    displayToValueMap = new LinkedHashMap<>();
                    for (Map.Entry<String, String> entry : enums.entrySet()) {
                        String display = entry.getKey() + " (" + entry.getValue() + ")";
                        displayToValueMap.put(display, entry.getKey());
                    }
                    comboBox = new ComboBox<>(displayToValueMap.keySet().toArray(new String[0]));
                    comboBox.setEditable(true);

                    // Try to select the correct display value
                    String valStr = String.valueOf(value);
                    String currentDisplay = displayToValueMap.entrySet().stream()
                            .filter(e -> e.getValue().equals(valStr))
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse(valStr);  // Fallback to raw value
                    comboBox.setSelectedItem(currentDisplay);

                    // Commit on Enter
                    comboBox.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
                        @Override
                        public void keyPressed(java.awt.event.KeyEvent e) {
                            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                                stopCellEditing();
                            }
                        }
                    });
                    return comboBox;
                } else {
                    return super.getTableCellEditorComponent(tbl, value, isSelected, row, column);
                }
            }

            @Override
            public Object getCellEditorValue() {
                if (comboBox != null && comboBox.isDisplayable()) {
                    Object selected = comboBox.getEditor().getItem();
                    String selectedStr = (selected != null) ? selected.toString() : "";
                    String rawValue = (displayToValueMap != null)
                            ? displayToValueMap.getOrDefault(selectedStr, selectedStr)
                            : selectedStr;
                    comboBox = null;
                    displayToValueMap = null;
                    return rawValue;
                } else {
                    return super.getCellEditorValue();
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(e -> notifySelection());
        table.getColumnModel().getSelectionModel().addListSelectionListener(e -> notifySelection());

        setupHeaderContextMenu();
        customizeTableHeaderWithIcon();
    }

    public void updateTable(List<String> fixMessages) {
        this.messages = new ArrayList<>(fixMessages);
        model.updateMessages(fixMessages);
    }

    public void setOnCellSelected(Runnable callback) {
        this.onCellSelectedCallback = callback;
    }

    private void notifySelection() {
        if (onCellSelectedCallback != null) onCellSelectedCallback.run();
    }

    public String getSelectedTag() {
        int row = table.getSelectedRow();
        return row >= 0 ? model.getTagAtRow(row) : null;
    }

    public String getSelectedMessageId() {
        int col = table.getSelectedColumn();
        return col >= 2 ? model.getMessageIdForColumn(col) : null;
    }

    public String getMessageText(String messageId) {
        if (messageId == null || !messageId.startsWith("Message ")) return "";
        try {
            int idx = Integer.parseInt(messageId.substring(8)) - 1;
            return (idx >= 0 && idx < messages.size()) ? messages.get(idx) : "";
        } catch (NumberFormatException ignore) {
            return "";
        }
    }

    public void highlightTagCell(String tag, String messageId) {
        int row = model.getRowForTag(tag);
        int col = model.getColumnForMessageId(messageId);
        if (row >= 0 && col >= 0) table.changeSelection(row, col, false, false);
        else if (row >= 0) table.setRowSelectionInterval(row, row);
        else table.clearSelection();
    }

    public void clearHighlight() {
        table.clearSelection();
    }

    private void setupHeaderContextMenu() {
        JTableHeader header = table.getTableHeader();
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int columnIndex = header.columnAtPoint(e.getPoint());
                    if (columnIndex != -1) {
                        showHeaderContextMenu(e, columnIndex);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressed(e);
            }
        });
    }

    private void showHeaderContextMenu(MouseEvent e, int columnIndex) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem hideColumn = new JMenuItem("Hide Column");
        hideColumn.addActionListener(ae -> hideColumn(columnIndex));
        menu.add(hideColumn);

        JMenuItem showOnlyColumn = new JMenuItem("Show Only This Column");
        showOnlyColumn.addActionListener(ae -> showOnlyColumn(columnIndex));
        menu.add(showOnlyColumn);

        JMenuItem showAllColumns = new JMenuItem("Show All Columns");
        showAllColumns.addActionListener(ae -> showAllColumns());
        menu.add(showAllColumns);

        if (columnIndex >= 2) {
            JMenuItem compareWith = new JMenuItem("Compare With...");
            compareWith.addActionListener(ae -> showCompareDialog(columnIndex));
            menu.add(compareWith);
        }

        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void hideColumn(int columnIndex) {
        String columnName = table.getColumnName(columnIndex);
        if ("Tag".equals(columnName) || "Name".equals(columnName)) {
            // Prevent hiding Tag or Name columns
            return;
        }

        TableColumnModel columnModel = table.getColumnModel();
        TableColumn column = columnModel.getColumn(columnIndex);
        columnModel.removeColumn(column);
        if (!allColumns.contains(column)) {
            allColumns.add(column);
        }
    }

    private void showOnlyColumn(int columnIndex) {
        TableColumnModel columnModel = table.getColumnModel();
        List<TableColumn> toRemove = new ArrayList<>();

        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            String columnName = table.getColumnName(i);
            if (i != columnIndex && !"Tag".equals(columnName) && !"Name".equals(columnName)) {
                toRemove.add(columnModel.getColumn(i));
            }
        }

        for (TableColumn column : toRemove) {
            columnModel.removeColumn(column);
            if (!allColumns.contains(column)) {
                allColumns.add(column);
            }
        }

        // Ensure Tag and Name are always shown
        for (TableColumn column : allColumns) {
            String columnName = column.getHeaderValue().toString();
            if (("Tag".equals(columnName) || "Name".equals(columnName)) && isColumnMissing(columnModel, column)) {
                columnModel.addColumn(column);
            }
        }
    }

    private void showAllColumns() {
        TableColumnModel columnModel = table.getColumnModel();
        for (TableColumn column : allColumns) {
            if (isColumnMissing(columnModel, column)) {
                columnModel.addColumn(column);
            }
        }
    }

    private void showCompareDialog(int columnIndex) {
        String firstId = model.getMessageIdForColumn(columnIndex);
        if (firstId == null) return;
        TableColumnModel columnModel = table.getColumnModel();
        List<String> options = new ArrayList<>();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            if (i == columnIndex) continue;
            String id = model.getMessageIdForColumn(i);
            if (id != null) options.add(id);
        }
        if (options.isEmpty()) return;

        String secondId = Messages.showEditableChooseDialog(
                "Select message to compare with",
                "Compare Messages",
                null,
                options.toArray(new String[0]),
                options.get(0),
                null
        );
        if (secondId == null) return;

        String text1 = getMessageText(firstId);
        String text2 = getMessageText(secondId);

        var contentFactory = DiffContentFactory.getInstance();
        var content1 = contentFactory.create(project, text1, FixFileType.INSTANCE);
        var content2 = contentFactory.create(project, text2, FixFileType.INSTANCE);
        var request = new SimpleDiffRequest("Compare FIX Messages", content1, content2, firstId, secondId);
        DiffManager.getInstance().showDiff(project, request);
    }

    private boolean isColumnMissing(TableColumnModel columnModel, TableColumn column) {
        Enumeration<TableColumn> columns = columnModel.getColumns();
        while (columns.hasMoreElements()) {
            if (columns.nextElement() == column) {
                return false;
            }
        }
        return true;
    }

    private void customizeTableHeaderWithIcon() {
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                Icon icon = UIManager.getIcon("Tree.expandedIcon"); // Or use your custom icon
                label.setIcon(icon);
                label.setHorizontalTextPosition(SwingConstants.LEFT);
                return label;
            }
        });
    }
}
