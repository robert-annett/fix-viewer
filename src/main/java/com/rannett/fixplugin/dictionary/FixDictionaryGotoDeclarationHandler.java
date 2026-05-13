package com.rannett.fixplugin.dictionary;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandlerBase;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FixDictionaryGotoDeclarationHandler extends GotoDeclarationHandlerBase {
    @Override
    public @Nullable PsiElement getGotoDeclarationTarget(@Nullable PsiElement sourceElement, @NotNull Editor editor) {
        XmlTag caretRefTag = findReferenceTagAtCaret(editor);
        if (caretRefTag != null) {
            PsiElement resolvedFromTag = resolveFromReferenceTag(caretRefTag);
            if (resolvedFromTag != null) {
                return resolvedFromTag;
            }
        }

        PsiElement lookupElement = normalizeSourceElement(sourceElement, editor);
        XmlAttributeValue attributeValue = findAttributeValue(lookupElement);
        if (attributeValue == null) {
            XmlTag fieldTag = findReferenceTagAtCaret(editor);
            if (fieldTag != null) {
                XmlAttribute fieldNameAttribute = fieldTag.getAttribute("name");
                if (fieldNameAttribute != null && fieldNameAttribute.getValueElement() != null) {
                    attributeValue = fieldNameAttribute.getValueElement();
                }
            }
            if (attributeValue == null) {
                return null;
            }
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
        if (!"field".equals(fieldRefTag.getName()) && !"component".equals(fieldRefTag.getName())) {
            return null;
        }
        return resolveFromReferenceTag(fieldRefTag);
    }

    @Override
    public @Nullable String getActionText(@NotNull DataContext context) {
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

    private PsiElement normalizeSourceElement(PsiElement sourceElement, Editor editor) {
        if (sourceElement != null) {
            return sourceElement;
        }
        PsiFile psiFile = editor.getProject() == null ? null : com.intellij.psi.PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        Document document = editor.getDocument();
        int textLength = document.getTextLength();
        if (offset > 0 && offset <= textLength) {
            PsiElement left = psiFile.findElementAt(offset - 1);
            if (left != null) {
                return left;
            }
        }
        if (offset < textLength) {
            PsiElement right = psiFile.findElementAt(offset);
            if (right != null) {
                return right;
            }
        }
        return psiFile.findElementAt(Math.max(0, Math.min(offset, Math.max(textLength - 1, 0))));
    }

    private XmlTag findReferenceTagAtCaret(Editor editor) {
        if (editor.getProject() == null) {
            return null;
        }
        PsiFile psiFile = com.intellij.psi.PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement current = psiFile.findElementAt(offset);
        XmlTag tag = PsiTreeUtil.getParentOfType(current, XmlTag.class, false);
        if (tag == null && offset > 0) {
            PsiElement left = psiFile.findElementAt(offset - 1);
            tag = PsiTreeUtil.getParentOfType(left, XmlTag.class, false);
        }
        if (tag == null || (!"field".equals(tag.getName()) && !"component".equals(tag.getName()))) {
            return null;
        }
        XmlTag parent = tag.getParentTag();
        if (parent == null || "fields".equals(parent.getName())) {
            return null;
        }
        return tag;
    }

    private PsiElement resolveFromReferenceTag(XmlTag fieldRefTag) {
        String fieldName = fieldRefTag.getAttributeValue("name");
        if (fieldName == null || fieldName.isBlank()) {
            return null;
        }
        if (!FixDictionaryXmlUtil.isFixDictionaryText(fieldRefTag.getContainingFile().getText())) {
            return null;
        }
        XmlTag rootTag = fieldRefTag;
        while (rootTag != null && !"fix".equalsIgnoreCase(rootTag.getName())) {
            rootTag = rootTag.getParentTag();
        }
        if (rootTag == null) {
            return null;
        }
        if ("field".equals(fieldRefTag.getName())) {
            XmlTag fieldsTag = rootTag.findFirstSubTag("fields");
            if (fieldsTag == null) {
                return null;
            }
            for (XmlTag fieldTag : fieldsTag.findSubTags("field")) {
                if (fieldName.equals(fieldTag.getAttributeValue("name"))) {
                    XmlAttribute targetNameAttribute = fieldTag.getAttribute("name");
                    return targetNameAttribute != null ? targetNameAttribute : fieldTag;
                }
            }
        }
        if ("component".equals(fieldRefTag.getName())) {
            XmlTag componentsTag = rootTag.findFirstSubTag("components");
            if (componentsTag == null) {
                return null;
            }
            for (XmlTag componentTag : componentsTag.findSubTags("component")) {
                if (fieldName.equals(componentTag.getAttributeValue("name"))) {
                    XmlAttribute targetNameAttribute = componentTag.getAttribute("name");
                    return targetNameAttribute != null ? targetNameAttribute : componentTag;
                }
            }
        }
        return null;
    }
}
