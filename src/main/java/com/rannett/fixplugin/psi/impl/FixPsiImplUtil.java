package com.rannett.fixplugin.psi.impl;

import com.intellij.lang.ASTNode;
import com.rannett.fixplugin.psi.FixField;
import com.rannett.fixplugin.psi.FixTypes;

public class FixPsiImplUtil {
    public static String getTag(FixField element) {
        ASTNode keyNode = element.getNode().findChildByType(FixTypes.TAG);
        if (keyNode != null) {
            // IMPORTANT: Convert embedded escaped spaces to simple spaces
            return keyNode.getText().replaceAll("\\\\ ", " ");
        } else {
            return null;
        }
    }

    public static String getValue(FixField element) {
        ASTNode valueNode = element.getNode().findChildByType(FixTypes.VALUE);
        if (valueNode != null) {
            return valueNode.getText();
        } else {
            return null;
        }
    }
}
