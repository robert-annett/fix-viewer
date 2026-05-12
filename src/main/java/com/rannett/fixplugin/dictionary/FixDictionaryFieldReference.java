package com.rannett.fixplugin.dictionary;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FixDictionaryFieldReference extends PsiReferenceBase<XmlAttributeValue> {

    public FixDictionaryFieldReference(@NotNull XmlAttributeValue element, @NotNull TextRange rangeInElement) {
        super(element, rangeInElement);
    }

    @Override
    public @Nullable PsiElement resolve() {
        PsiElement parent = myElement.getParent();
        if (!(parent instanceof XmlAttribute attribute)) {
            return null;
        }

        String fieldName = attribute.getValue();
        if (fieldName == null || fieldName.isBlank()) {
            return null;
        }

        if (attribute.getParent() == null) {
            return null;
        }
        XmlTag rootTag = attribute.getParent().getParentTag();
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
            String name = fieldTag.getAttributeValue("name");
            if (fieldName.equals(name)) {
                XmlAttribute nameAttribute = fieldTag.getAttribute("name");
                return nameAttribute != null ? nameAttribute : fieldTag;
            }
        }
        return null;
    }
}
