package com.rannett.fixplugin.ui;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for the FIX Field Lookup tool window.
 */
public class FixFieldLookupToolWindowFactory implements ToolWindowFactory, DumbAware {

    /**
     * Creates the tool window content.
     *
     * @param project    current project
     * @param toolWindow tool window instance
     */
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        FixFieldLookupPanel panel = new FixFieldLookupPanel(project);
        Content content = ContentFactory.getInstance().createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}

