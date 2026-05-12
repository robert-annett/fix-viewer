package com.rannett.fixplugin.dictionary;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandlerBase;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FixDictionaryGotoDeclarationHandler extends GotoDeclarationHandlerBase {
    @Override
    public @Nullable PsiElement getGotoDeclarationTarget(@Nullable PsiElement sourceElement, @NotNull Editor editor) {
        XmlAttributeValue attributeValue = findAttributeValue(sourceElement);
        if (attributeValue == null) {
            return null;
        }
        if (!(attributeValue.getParent() instanceof XmlAttribute attribute)) {
            return null;
        }
        if (!"name".equals(attribute.getName())) {
            return null;
        }
        if (attribute.getParent() == null || attribute.getParent().getParentTag() == null) {
            return null;
        }

        XmlTag fieldRefTag = attribute.getParent().getParentTag();
        if (!"field".equals(fieldRefTag.getName())) {
            return null;
        }
        XmlTag containerTag = fieldRefTag.getParentTag();
        if (containerTag == null || (!"message".equals(containerTag.getName()) && !"group".equals(containerTag.getName()))) {
            return null;
        }

        String fieldName = attribute.getValue();
        if (fieldName == null || fieldName.isBlank()) {
            return null;
        }
        String fileText = attribute.getContainingFile().getText();
        if (!FixDictionaryXmlUtil.isFixDictionaryText(fileText)) {
            return null;
        }

        XmlTag rootTag = containerTag;
        while (rootTag != null && !"fix".equalsIgnoreCase(rootTag.getName())) {
            rootTag = rootTag.getParentTag();
        }
        if (rootTag == null) {
            return null;
        }

        XmlTag fieldsTag = rootTag.findFirstSubTag("fields");
        if (fieldsTag == null) {
            return null;
        }
        for (XmlTag fieldTag : fieldsTag.findSubTags("field")) {
            if (fieldName.equals(fieldTag.getAttributeValue("name"))) {
                XmlAttribute targetNameAttribute = fieldTag.getAttribute("name");
                return targetNameAttribute != null ? targetNameAttribute.getValueElement() : fieldTag;
            }
        }
        return null;
    }

    @Override
    public @Nullable String getActionText(DataContext context) {
        return null;
    }

    private XmlAttributeValue findAttributeValue(PsiElement sourceElement) {
        PsiElement current = sourceElement;
        while (current != null) {
            if (current instanceof XmlAttributeValue value) {
                return value;
            }
            current = current.getParent();
        }
        return null;
    }
}
