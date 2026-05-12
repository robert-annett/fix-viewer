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
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
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
        if (psiFile == null) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        PsiElement sourceElement = psiFile.findElementAt(editor.getCaretModel().getOffset());
        PsiElement target = resolveTarget(sourceElement);
        event.getPresentation().setEnabledAndVisible(target != null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private PsiElement resolveTarget(PsiElement sourceElement) {
        if (!(sourceElement instanceof XmlAttributeValue attributeValue)) {
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

        if (!FixDictionaryXmlUtil.isFixDictionaryText(attribute.getContainingFile().getText())) {
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
}
