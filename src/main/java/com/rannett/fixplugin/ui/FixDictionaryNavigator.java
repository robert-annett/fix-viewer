package com.rannett.fixplugin.ui;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.rannett.fixplugin.settings.FixViewerSettingsState;
import com.rannett.fixplugin.settings.FixViewerSettingsState.DictionaryEntry;

import java.io.File;

public final class FixDictionaryNavigator {
    private FixDictionaryNavigator() {
    }

    public static boolean navigateToTag(Project project, String fixVersion, String messageType, String tagNumber) {
        if (project == null || tagNumber == null || tagNumber.isBlank()) {
            return false;
        }
        XmlTag root = resolveDictionaryRoot(project, fixVersion);
        if (root == null) {
            return false;
        }
        String fieldName = findFieldNameByTag(root, tagNumber);
        if (fieldName == null) {
            return false;
        }
        XmlAttribute target = resolveMessageFieldAttribute(root, messageType, fieldName);
        if (target == null) {
            target = resolveGlobalFieldAttribute(root, fieldName);
        }
        if (target == null || target.getContainingFile() == null || target.getContainingFile().getVirtualFile() == null) {
            return false;
        }
        VirtualFile targetFile = target.getContainingFile().getVirtualFile();
        FileEditorManager.getInstance(project).openTextEditor(
                new OpenFileDescriptor(project, targetFile, target.getTextRange().getStartOffset()), true);
        return true;
    }

    private static XmlTag resolveDictionaryRoot(Project project, String fixVersion) {
        DictionaryEntry entry = FixViewerSettingsState.getInstance(project).getDefaultDictionary(fixVersion);
        VirtualFile vf = null;
        if (entry != null && !entry.isBuiltIn() && entry.getPath() != null && !entry.getPath().isBlank()) {
            vf = LocalFileSystem.getInstance().findFileByIoFile(new File(entry.getPath()));
        }
        if (vf == null) {
            java.net.URL dictionaryUrl = FixDictionaryNavigator.class.getResource("/dictionaries/" + fixVersion + ".xml");
            if (dictionaryUrl != null) {
                vf = com.intellij.openapi.vfs.VfsUtil.findFileByURL(dictionaryUrl);
            }
        }
        if (vf == null) {
            return null;
        }
        PsiFile psi = PsiManager.getInstance(project).findFile(vf);
        if (psi == null) {
            return null;
        }
        if (!(psi instanceof XmlFile xmlFile)) {
            return null;
        }
        XmlTag root = xmlFile.getRootTag();
        if (root != null && "fix".equalsIgnoreCase(root.getName())) {
            return root;
        }
        return null;
    }

    private static String findFieldNameByTag(XmlTag root, String tagNumber) {
        XmlTag fields = root.findFirstSubTag("fields");
        if (fields == null) {
            return null;
        }
        for (XmlTag tag : fields.findSubTags("field")) {
            if (tagNumber.equals(tag.getAttributeValue("number"))) {
                return tag.getAttributeValue("name");
            }
        }
        return null;
    }

    private static XmlAttribute resolveMessageFieldAttribute(XmlTag root, String messageType, String fieldName) {
        if (messageType == null || messageType.isBlank()) {
            return null;
        }
        XmlTag messages = root.findFirstSubTag("messages");
        if (messages == null) {
            return null;
        }
        for (XmlTag message : messages.findSubTags("message")) {
            if (!messageType.equals(message.getAttributeValue("msgtype"))) {
                continue;
            }
            for (XmlTag field : message.findSubTags("field")) {
                if (fieldName.equals(field.getAttributeValue("name"))) {
                    XmlAttribute nameAttribute = field.getAttribute("name");
                    if (nameAttribute != null) {
                        return nameAttribute;
                    }
                }
            }
        }
        return null;
    }

    private static XmlAttribute resolveGlobalFieldAttribute(XmlTag root, String fieldName) {
        XmlTag fields = root.findFirstSubTag("fields");
        if (fields == null) {
            return null;
        }
        for (XmlTag field : fields.findSubTags("field")) {
            if (fieldName.equals(field.getAttributeValue("name"))) {
                return field.getAttribute("name");
            }
        }
        return null;
    }
}
