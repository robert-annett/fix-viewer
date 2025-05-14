package com.rannett.fixplugin.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.rannett.fixplugin.FixElementFactory;
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

    public static PsiElement setValue(FixField element, String newValue) {
        ASTNode keyNode = element.getNode().findChildByType(FixTypes.TAG);
        ASTNode valueNode = element.getNode().findChildByType(FixTypes.VALUE);
        if (keyNode != null) {
            FixField property = FixElementFactory.createFixField(element.getProject(), "10", newValue);
            ASTNode newValueNode = property.getLastChild().getNode();
            element.getNode().replaceChild(valueNode, newValueNode);
        }
        return element;
    }

}
