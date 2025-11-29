package com.rannett.fixplugin.ui.dictionary;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Action that forces a file to open using the FIX dictionary viewer.
 */
public class OpenAsFixDictionaryAction extends AnAction implements DumbAware {

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) {
            return;
        }
        file.putUserData(FixDictionaryDetector.FORCE_DICTIONARY_VIEWER, true);
        FileEditorManager manager = FileEditorManager.getInstance(project);
        manager.closeFile(file);
        manager.openFile(file, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean enabled = file != null && "xml".equalsIgnoreCase(file.getExtension());
        event.getPresentation().setEnabledAndVisible(enabled);
    }
}
