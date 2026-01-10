package com.rannett.fixplugin.ui;

import javax.swing.tree.DefaultMutableTreeNode;

final class FixTimelineMessageNode extends DefaultMutableTreeNode {
    final int index;
    final String time;
    final String direction;
    final String msgTypeCode;
    final String msgTypeDisplay;

    FixTimelineMessageNode(int index, String time, String direction, String msgTypeCode, String msgTypeDisplay, String summary) {
        super(summary);
        this.index = index;
        this.time = time;
        this.direction = direction;
        this.msgTypeCode = msgTypeCode;
        this.msgTypeDisplay = msgTypeDisplay;
    }
}
