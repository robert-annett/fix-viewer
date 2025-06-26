package com.rannett.fixplugin;

import com.intellij.lang.documentation.DocumentationImageResolver;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.model.Pointer;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.platform.backend.documentation.DocumentationContent;
import com.intellij.platform.backend.documentation.DocumentationResult;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.platform.backend.presentation.TargetPresentationBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * DocumentationTarget implementation for FIX elements.
 */
public class FixDocumentationTarget implements DocumentationTarget {
    private final @NotNull PsiElement element;
    private final @NotNull Project project;

    public FixDocumentationTarget(@NotNull PsiElement element) {
        this.element = element;
        this.project = element.getProject();
    }

    @Override
    public @NotNull Pointer<? extends DocumentationTarget> createPointer() {
        SmartPsiElementPointer<PsiElement> pointer = SmartPointerManager.getInstance(project)
                .createSmartPsiElementPointer(element);
        return () -> {
            PsiElement psi = pointer.getElement();
            return psi == null ? null : new FixDocumentationTarget(psi);
        };
    }

    @Override
    public @NotNull TargetPresentation computePresentation() {
        return TargetPresentation.builder(element.getText()).presentation();
    }

    @Override
    public @Nullable Navigatable getNavigatable() {
        return element instanceof Navigatable ? (Navigatable) element : null;
    }

    @Override
    public @Nullable String computeDocumentationHint() {
        return element.getText();
    }

    @Override
    @RequiresReadLock
    public @Nullable DocumentationResult computeDocumentation() {
        String html = new FixDocumentationProvider().generateDoc(element, element);
        if (html == null) {
            return null;
        }
        DocumentationContent content = DocumentationContent.content(html);
        return DocumentationResult.documentation(content);
    }
}
