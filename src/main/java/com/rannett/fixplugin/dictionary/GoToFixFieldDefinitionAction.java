package com.rannett.fixplugin.dictionary;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class GoToFixFieldDefinitionAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) {
            return;
        }

        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return;
        }
        PsiElement sourceElement = psiFile.findElementAt(editor.getCaretModel().getOffset());
        PsiElement target = resolveTarget(sourceElement);
        if (target instanceof NavigationItem navigationItem) {
            navigationItem.navigate(true);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null && project != null) {
            psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        }
        if (psiFile == null) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        XmlTag fieldTag = findFieldTagAtCaret(psiFile, editor.getCaretModel().getOffset());
        event.getPresentation().setEnabledAndVisible(fieldTag != null && FixDictionaryXmlUtil.isFixDictionaryText(psiFile.getText()));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private PsiElement resolveTarget(PsiElement sourceElement) {
        XmlTag tagFallback = PsiTreeUtil.getParentOfType(sourceElement, XmlTag.class);
        if (tagFallback != null && "field".equals(tagFallback.getName())) {
            PsiElement resolved = resolveFromFieldTag(tagFallback);
            if (resolved != null) {
                return resolved;
            }
        }
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
        return resolveFromFieldTag(fieldRefTag);
    }

    private PsiElement resolveFromFieldTag(XmlTag fieldRefTag) {
        XmlTag containerTag = fieldRefTag.getParentTag();
        if (containerTag == null || "fields".equals(containerTag.getName())) {
            return null;
        }
        String fieldName = fieldRefTag.getAttributeValue("name");
        if (fieldName == null || fieldName.isBlank()) {
            return null;
        }
        if (!FixDictionaryXmlUtil.isFixDictionaryText(fieldRefTag.getContainingFile().getText())) {
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
                return targetNameAttribute != null ? targetNameAttribute : fieldTag;
            }
        }
        return null;
    }

    private XmlTag findFieldTagAtCaret(PsiFile psiFile, int offset) {
        PsiElement element = psiFile.findElementAt(offset);
        XmlTag tag = PsiTreeUtil.getParentOfType(element, XmlTag.class);
        if (tag == null && offset > 0) {
            tag = PsiTreeUtil.getParentOfType(psiFile.findElementAt(offset - 1), XmlTag.class);
        }
        if (tag == null || !"field".equals(tag.getName())) {
            return null;
        }
        XmlTag parent = tag.getParentTag();
        if (parent == null || "fields".equals(parent.getName())) {
            return null;
        }
        return tag;
    }

    private XmlAttributeValue findAttributeValue(PsiElement sourceElement) {
        PsiElement cursor = sourceElement;
        while (cursor != null) {
            if (cursor instanceof XmlAttributeValue value) {
                return value;
            }
            cursor = cursor.getParent();
        }
        return null;
    }
}
