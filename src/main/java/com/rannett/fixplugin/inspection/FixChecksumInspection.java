package com.rannett.fixplugin.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.rannett.fixplugin.psi.FixField;
import org.jetbrains.annotations.NotNull;

public class FixChecksumInspection extends LocalInspectionTool  {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                                   boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (!(element instanceof FixField)) return;

                FixField field = (FixField) element;
                if (!"10".equals(field.getTag())) return;

                PsiFile file = element.getContainingFile();
                String fileText = file.getText();
                int checksumStart = field.getTextOffset();
                String messageUpToChecksum = fileText.substring(0, checksumStart);

                int calculated = calculateChecksum(messageUpToChecksum);
                String expected = String.format("%03d", calculated);
                String actual = field.getValue();

                if (!expected.equals(actual)) {
                    holder.registerProblem(
                            field,
                            "Incorrect checksum (expected " + expected + ")",
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            new FixChecksumQuickFix(expected)
                    );
                }
            }
        };
    }

    private int calculateChecksum(String textUpToChecksum) {
        int sum = 0;
        for (char c : textUpToChecksum.toCharArray()) {
            sum += c;
        }
        return sum % 256;
    }
}

