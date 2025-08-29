package com.rannett.fixplugin.ui;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.rannett.fixplugin.util.FixMessageParser;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import quickfix.DataDictionary;
import quickfix.Message;

/**
 * Panel that renders a simple timeline view of FIX messages.
 */
public class FixCommTimelinePanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;
    private final JCheckBox hideHeartbeat;
    private final List<RowData> allRows = new ArrayList<>();
    private final List<RowData> displayedRows = new ArrayList<>();
    private Consumer<Integer> onMessageSelected;

    public FixCommTimelinePanel(@NotNull List<String> messages) {
        super(new BorderLayout());
        model = new DefaultTableModel(new Object[]{"Time", "Dir", "MsgType", "Summary"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JBTable(model);
        hideHeartbeat = new JCheckBox("Hide heartbeats");
        hideHeartbeat.addActionListener(e -> applyFilter());
        JScrollPane scroll = new JBScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(hideHeartbeat, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> notifySelection());
        loadMessages(messages);
    }

    /**
     * Update the timeline with new messages.
     */
    public void updateMessages(@NotNull List<String> messages) {
        loadMessages(messages);
    }

    /**
     * Configure a callback when a message row is selected.
     *
     * @param callback consumer receiving the 1-based message index
     */
    public void setOnMessageSelected(Consumer<Integer> callback) {
        this.onMessageSelected = callback;
    }

    /**
     * Set whether heartbeat messages should be hidden.
     */
    public void setHideHeartbeats(boolean hide) {
        hideHeartbeat.setSelected(hide);
        applyFilter();
    }

    /**
     * Return the number of visible rows.
     */
    public int getVisibleRowCount() {
        return model.getRowCount();
    }

    private void loadMessages(List<String> messages) {
        allRows.clear();
        IntStream.range(0, messages.size()).forEach(i -> {
            RowData row = parseRow(messages.get(i), i + 1);
            allRows.add(row);
        });
        applyFilter();
    }

    private void applyFilter() {
        model.setRowCount(0);
        displayedRows.clear();
        allRows.stream()
                .filter(r -> !hideHeartbeat.isSelected() || !"0".equals(r.msgType))
                .forEach(r -> {
                    displayedRows.add(r);
                    model.addRow(new Object[]{r.time, r.direction, r.msgType, r.summary});
                });
    }

    private void notifySelection() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < displayedRows.size() && onMessageSelected != null) {
            onMessageSelected.accept(displayedRows.get(row).index);
        }
    }

    private RowData parseRow(String msg, int index) {
        String begin = extractBeginString(msg);
        try {
            DataDictionary dd = FixMessageParser.loadDataDictionary(begin, null);
            Message parsed = FixMessageParser.parse(msg, dd);
            String time = parsed.getHeader().isSetField(52) ? parsed.getHeader().getString(52) : "";
            String type = parsed.getHeader().isSetField(35) ? parsed.getHeader().getString(35) : "";
            String summary = FixMessageParser.buildMessageLabel(parsed, dd);
            return new RowData(index, time, "→", type, summary);
        } catch (Exception e) {
            return new RowData(index, "", "→", "", msg);
        }
    }

    private static String extractBeginString(String msg) {
        int start = msg.indexOf("8=");
        if (start >= 0) {
            int pipe = msg.indexOf('|', start);
            int soh = msg.indexOf('\u0001', start);
            int end = pipe >= 0 && (soh < 0 || pipe < soh) ? pipe : soh;
            if (end > start) {
                return msg.substring(start + 2, end);
            }
        }
        return "FIX.4.4";
    }

    private static final class RowData {
        final int index;
        final String time;
        final String direction;
        final String msgType;
        final String summary;

        RowData(int index, String time, String direction, String msgType, String summary) {
            this.index = index;
            this.time = time;
            this.direction = direction;
            this.msgType = msgType;
            this.summary = summary;
        }
    }
}

