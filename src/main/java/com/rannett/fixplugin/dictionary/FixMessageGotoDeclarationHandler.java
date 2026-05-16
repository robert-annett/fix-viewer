package com.rannett.fixplugin.dictionary;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandlerBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.rannett.fixplugin.psi.FixTypes;
import com.rannett.fixplugin.settings.FixViewerSettingsState;
import com.rannett.fixplugin.util.FixUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class FixMessageGotoDeclarationHandler extends GotoDeclarationHandlerBase {
    @Override
    public @Nullable PsiElement getGotoDeclarationTarget(@Nullable PsiElement sourceElement, @NotNull Editor editor) {
        if (sourceElement == null || sourceElement.getNode() == null || sourceElement.getContainingFile() == null) {
            return null;
        }
        if (sourceElement.getNode().getElementType() != FixTypes.TAG) {
            return null;
        }

        String tagText = sourceElement.getText();
        if (!tagText.matches("\\d+")) {
            return null;
        }

        Project project = editor.getProject();
        if (project == null) {
            return null;
        }

        PsiFile fixFile = sourceElement.getContainingFile();
        String fixVersion = FixUtils.extractFixVersion(fixFile.getText()).orElse("FIXT.1.1");
        String messageType = findCurrentMessageType(editor, fixFile);

        XmlTag dictionaryRoot = resolveDictionaryRoot(project, fixVersion);
        if (dictionaryRoot == null) {
            return null;
        }

        String fieldName = findFieldNameByTagNumber(dictionaryRoot, tagText);
        if (fieldName == null) {
            return null;
        }

        PsiElement messageField = resolveFieldInMessage(dictionaryRoot, messageType, fieldName);
        if (messageField != null) {
            return messageField;
        }

        return resolveFieldDefinition(dictionaryRoot, fieldName);
    }

    private String findCurrentMessageType(Editor editor, PsiFile file) {
        String text = file.getText();
        int caretOffset = editor.getCaretModel().getOffset();
        int start = text.lastIndexOf("8=", Math.max(0, caretOffset));
        if (start < 0) {
            start = 0;
        }
        int end = text.indexOf("10=", caretOffset);
        if (end < 0) {
            end = text.length();
        } else {
            int checksumEnd = text.indexOf('\u0001', end);
            if (checksumEnd > end) {
                end = checksumEnd;
            }
        }
        String segment = text.substring(start, Math.min(end, text.length()));
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?:^|[\\u0001|])35=([^\\u0001|]+)").matcher(segment);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private XmlTag resolveDictionaryRoot(Project project, String fixVersion) {
        FixViewerSettingsState.DictionaryEntry entry = FixViewerSettingsState.getInstance(project).getDefaultDictionary(fixVersion);
        VirtualFile virtualFile = null;
        if (entry != null && !entry.isBuiltIn() && entry.getPath() != null && !entry.getPath().isBlank()) {
            virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(entry.getPath()));
        }
        if (virtualFile == null) {
            try {
                java.net.URL dictionaryUrl = FixMessageGotoDeclarationHandler.class.getResource("/dictionaries/" + fixVersion + ".xml");
                if (dictionaryUrl != null) {
                    virtualFile = com.intellij.openapi.vfs.VfsUtil.findFileByURL(dictionaryUrl);
                }
            } catch (Throwable ignored) {
                return null;
            }
        }
        if (virtualFile == null) {
            return null;
        }
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) {
            return null;
        }
        return findFixRootTag(psiFile);
    }

    private XmlTag findFixRootTag(PsiFile psiFile) {
        if (!(psiFile instanceof XmlFile xmlFile)) {
            return null;
        }
        XmlTag root = xmlFile.getRootTag();
        if (root != null && "fix".equalsIgnoreCase(root.getName())) {
            return root;
        }
        return null;
    }

    private String findFieldNameByTagNumber(XmlTag dictionaryRoot, String tagNumber) {
        XmlTag fieldsTag = dictionaryRoot.findFirstSubTag("fields");
        if (fieldsTag == null) {
            return null;
        }
        for (XmlTag fieldTag : fieldsTag.findSubTags("field")) {
            if (tagNumber.equals(fieldTag.getAttributeValue("number"))) {
                return fieldTag.getAttributeValue("name");
            }
        }
        return null;
    }

    private PsiElement resolveFieldInMessage(XmlTag dictionaryRoot, String messageType, String fieldName) {
        if (messageType == null || messageType.isBlank()) {
            return null;
        }
        XmlTag messagesTag = dictionaryRoot.findFirstSubTag("messages");
        if (messagesTag == null) {
            return null;
        }
        for (XmlTag messageTag : messagesTag.findSubTags("message")) {
            if (!messageType.equals(messageTag.getAttributeValue("msgtype"))) {
                continue;
            }
            List<XmlTag> matching = java.util.Arrays.stream(messageTag.findSubTags("field"))
                    .filter(fieldTag -> fieldName.equals(fieldTag.getAttributeValue("name")))
                    .toList();
            if (!matching.isEmpty()) {
                XmlAttribute nameAttribute = matching.get(0).getAttribute("name");
                return nameAttribute != null ? nameAttribute : matching.get(0);
            }
        }
        return null;
    }

    private PsiElement resolveFieldDefinition(XmlTag dictionaryRoot, String fieldName) {
        XmlTag fieldsTag = dictionaryRoot.findFirstSubTag("fields");
        if (fieldsTag == null) {
            return null;
        }
        for (XmlTag fieldTag : fieldsTag.findSubTags("field")) {
            if (fieldName.equals(fieldTag.getAttributeValue("name"))) {
                XmlAttribute nameAttribute = fieldTag.getAttribute("name");
                return nameAttribute != null ? nameAttribute : fieldTag;
            }
        }
        return null;
    }
}
