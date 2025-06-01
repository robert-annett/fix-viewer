package com.rannett.fixplugin;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.rannett.fixplugin.psi.FixField;
import com.rannett.fixplugin.psi.FixFile;

public class FixElementFactory {

    public static FixFile createFile(Project project, String text) {
        String name = "dummy.simple";
        return (FixFile) PsiFileFactory.getInstance(project).createFileFromText(name, FixFileType.INSTANCE, text);
    }

    public static FixField createFixField(Project project, String tag, String value) {
        final FixFile file = createFile(project, tag + "=" + value);
        return (FixField) file.getFirstChild();
    }
}
