package com.rannett.fixplugin.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.rannett.fixplugin.psi.FixField;
import com.rannett.fixplugin.psi.FixTypes;
import quickfix.field.EncodedSecurityDesc;
import quickfix.field.XmlData;
import org.jetbrains.annotations.NotNull;

public class FixInvalidCharAnnotator implements Annotator {

    /**
     * Annotates invalid characters within FIX message elements.
     *
     * @param element the PSI element to check
     * @param holder  the annotation holder used to report problems
     */
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {

        IElementType type = element.getNode().getElementType();

        if (type != FixTypes.TAG &&
                type != FixTypes.VALUE &&
                type != FixTypes.SEPARATOR &&
                type != FixTypes.FIELD_SEPARATOR) {
            return;
        }

        FixField field = PsiTreeUtil.getParentOfType(element, FixField.class);
        if (type == FixTypes.VALUE && field != null) {
            String tag = field.getTag();
            if (String.valueOf(XmlData.FIELD).equals(tag) ||
                    String.valueOf(EncodedSecurityDesc.FIELD).equals(tag)) {
                return;
            }
        }

        String text = element.getText();
        int baseOffset = element.getTextRange().getStartOffset();

        for (int i = 0; i < text.length(); i++) {
            int code = text.charAt(i);

            // Valid: ASCII 32–126 (printable) and SOH (ASCII 1)
            if (code != 1 && (code < 32 || code > 126)) {
                TextRange charRange = new TextRange(baseOffset + i, baseOffset + i + 1);

                holder.newAnnotation(HighlightSeverity.ERROR, "Invalid FIX character: ASCII " + code)
                        .range(charRange)
                        .textAttributes(DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)
                        .create();
            }
        }
    }
}

