package com.rannett.fixplugin.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.rannett.fixplugin.dictionary.FixDictionaryCache;
import com.rannett.fixplugin.dictionary.FixTagDictionary;
import com.rannett.fixplugin.psi.FixTypes;
import com.rannett.fixplugin.util.FieldTypeValidator;
import com.rannett.fixplugin.util.FixUtils;
import org.jetbrains.annotations.NotNull;

public class FixCheckTypeAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        IElementType type = element.getNode().getElementType();

        // Only check VALUE elements
        if (type != FixTypes.VALUE) {
            return;
        }

        PsiElement parent = element.getParent();
        if (parent == null) {
            return;
        }

        PsiElement tagElement = parent.getFirstChild();
        if (tagElement == null) {
            return;
        }

        String tagNumber = tagElement.getText();
        String value = element.getText();

        // Retrieve the version from the containing file or fallback
        PsiElement fileElement = element.getContainingFile();
        String version = FixUtils.extractFixVersion(fileElement.getText()).orElse("FIX.4.2"); // Fallback default


        FixTagDictionary dictionary = element.getProject().getService(FixDictionaryCache.class).getDictionary(version);
        String expectedType = dictionary.getFieldType(tagNumber);

        if (expectedType != null && !FieldTypeValidator.isValueValidForType(expectedType, value)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Invalid value for type " + expectedType)
                    .range(element.getTextRange())
                    .create();
        }
    }

}
