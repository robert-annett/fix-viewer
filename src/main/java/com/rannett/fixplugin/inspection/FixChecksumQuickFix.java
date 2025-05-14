package com.rannett.fixplugin.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class FixChecksumQuickFix implements LocalQuickFix {

    private final String correctChecksum;

    public FixChecksumQuickFix(String correctChecksum) {
        this.correctChecksum = correctChecksum;
    }

    @NotNull
    @Override
    public String getName() {
        return "Correct checksum to " + correctChecksum;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Fix incorrect checksum";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
//        PsiElement element = descriptor.getPsiElement();
//        if (!(element instanceof FixField)) return;
//
//        FixField field = (FixField) element;
//        FixValue valueElement = field.getValue();
//
//        if (valueElement == null) return;
//
//        FixValue newValue = FixElementFactory.createValue(project, correctChecksum);
//        if (newValue != null) {
//            valueElement.replace(newValue);
//        }
    }


}

