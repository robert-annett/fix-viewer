package com.rannett.fixplugin;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Provider that creates FixDocumentationTarget instances.
 */
public class FixDocumentationTargetProvider implements com.intellij.platform.backend.documentation.DocumentationTargetProvider {

    @Override
    @RequiresReadLock
    public @NotNull List<? extends com.intellij.platform.backend.documentation.DocumentationTarget> documentationTargets(@NotNull PsiFile file, int offset) {
        PsiElement element = file.findElementAt(offset);
        if (element == null) return Collections.emptyList();

        // use the same logic as FixDocumentationProvider#getCustomDocumentationElement
        var elementType = PsiUtilCore.getElementType(element);
        if (elementType == com.rannett.fixplugin.psi.FixTypes.TAG || elementType == com.rannett.fixplugin.psi.FixTypes.VALUE) {
            return List.of(new FixDocumentationTarget(element));
        }
        String text = element.getText();
        if (text.matches("\\d+.*") || text.matches("[A-Z]+")) {
            return List.of(new FixDocumentationTarget(element));
        }
        return Collections.emptyList();
    }
}
