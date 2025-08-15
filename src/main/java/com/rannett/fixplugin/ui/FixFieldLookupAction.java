package com.rannett.fixplugin.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

/**
 * Action to display the FIX Field Lookup tool window.
 */
public class FixFieldLookupAction extends AnAction implements DumbAware {

    /**
     * Opens or activates the tool window.
     *
     * @param e action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Fix Field Lookup");
        if (toolWindow != null) {
            toolWindow.show();
        }
    }
}

