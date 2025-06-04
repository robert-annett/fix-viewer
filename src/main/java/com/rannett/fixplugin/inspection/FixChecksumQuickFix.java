package com.rannett.fixplugin.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.rannett.fixplugin.psi.FixField;
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
        if (descriptor.getPsiElement() instanceof FixField field && field.getTag() != null) {
            field.setValue(correctChecksum);
        }
    }


}

