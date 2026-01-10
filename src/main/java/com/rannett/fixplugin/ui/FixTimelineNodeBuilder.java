package com.rannett.fixplugin.ui;

import com.rannett.fixplugin.settings.FixViewerSettingsState.DictionaryEntry;
import com.rannett.fixplugin.util.FixMessageParser;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import quickfix.DataDictionary;
import quickfix.Field;
import quickfix.FieldMap;
import quickfix.Group;
import quickfix.Message;

final class FixTimelineNodeBuilder {
    private final Map<String, String> localPartyBySession = new HashMap<>();

    void resetSessionParties() {
        localPartyBySession.clear();
    }

    FixTimelineMessageNode buildNode(String msg, int index) {
        String begin = extractBeginString(msg);
        try {
            DataDictionary dd = FixMessageParser.loadDataDictionary(begin, (DictionaryEntry) null);
            Message parsed = FixMessageParser.parse(msg, dd);

            String time = parsed.getHeader().isSetField(52) ? parsed.getHeader().getString(52) : "";
            String typeCode = parsed.getHeader().isSetField(35) ? parsed.getHeader().getString(35) : "";
            String typeName = buildTypeName(dd, typeCode);
            String sender = parsed.getHeader().isSetField(49) ? parsed.getHeader().getString(49) : "";
            String target = parsed.getHeader().isSetField(56) ? parsed.getHeader().getString(56) : "";
            String direction = determineDirection(sender, target);
            String summary = FixMessageParser.buildMessageLabel(parsed, dd);

            FixTimelineMessageNode node = new FixTimelineMessageNode(index, time, direction, typeCode, typeName, summary);
            DefaultMutableTreeNode headerNode = new DefaultMutableTreeNode("Header");
            buildNodes(parsed.getHeader(), headerNode, dd);
            node.add(headerNode);

            DefaultMutableTreeNode bodyNode = new DefaultMutableTreeNode("Body");
            buildNodes(parsed, bodyNode, dd);
            node.add(bodyNode);

            DefaultMutableTreeNode trailerNode = new DefaultMutableTreeNode("Trailer");
            buildNodes(parsed.getTrailer(), trailerNode, dd);
            node.add(trailerNode);
            return node;
        } catch (Exception e) {
            FixTimelineMessageNode node = new FixTimelineMessageNode(index, "", "→", "", "", msg);
            node.add(new DefaultMutableTreeNode("Parse error: " + e.getMessage()));
            return node;
        }
    }

    private static String buildTypeName(DataDictionary dd, String typeCode) {
        if (typeCode == null || typeCode.isEmpty()) {
            return "";
        }
        String name = dd.getValueName(35, typeCode);
        if (name != null && !name.isEmpty()) {
            return typeCode + " (" + name.toUpperCase() + ")";
        }
        return typeCode;
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

    private String determineDirection(String sender, String target) {
        if (sender.isEmpty() || target.isEmpty()) {
            return "→";
        }
        String key = sessionKey(sender, target);
        String local = localPartyBySession.get(key);
        if (local == null) {
            localPartyBySession.put(key, sender);
            local = sender;
        }
        if (sender.equals(local)) {
            return "→";
        }
        return "←";
    }

    private static String sessionKey(String a, String b) {
        if (a.compareTo(b) < 0) {
            return a + "|" + b;
        }
        return b + "|" + a;
    }

    private void buildNodes(FieldMap map, DefaultMutableTreeNode parent, DataDictionary dd) {
        java.util.Iterator<Field<?>> fieldIt = map.iterator();
        while (fieldIt.hasNext()) {
            Field<?> field = fieldIt.next();
            int tag = field.getTag();
            String name = dd.getFieldName(tag);
            String value = String.valueOf(field.getObject());
            String enumName = dd.getValueName(tag, value);

            StringBuilder label = new StringBuilder();
            label.append(tag).append("=").append(value);
            if (name != null) {
                label.append(" (").append(name);
                if (enumName != null) {
                    label.append("=").append(enumName);
                }
                label.append(")");
            }
            parent.add(new DefaultMutableTreeNode(label.toString()));
        }

        java.util.Iterator<Integer> groupKeys = map.groupKeyIterator();
        while (groupKeys.hasNext()) {
            int groupTag = groupKeys.next();
            List<Group> groups = map.getGroups(groupTag);
            String groupName = dd.getFieldName(groupTag);
            int idx = 1;
            for (Group g : groups) {
                String title = groupName != null ? groupName : String.valueOf(groupTag);
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(title + " [" + idx++ + "]");
                buildNodes(g, groupNode, dd);
                parent.add(groupNode);
            }
        }
    }
}
