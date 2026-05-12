package com.rannett.fixplugin.dictionary;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class FixDictionaryXmlReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(XmlAttribute.class),
                new com.intellij.psi.PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull com.intellij.psi.PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        XmlAttribute attribute = (XmlAttribute) element;
                        if (!"name".equals(attribute.getName())) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        if (attribute.getParent() == null || attribute.getParent().getParentTag() == null) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        if (!"field".equals(attribute.getParent().getParentTag().getName())) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        if (attribute.getParent().getParentTag().getParentTag() == null ||
                                !"message".equals(attribute.getParent().getParentTag().getParentTag().getName())) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        if (!FixDictionaryXmlUtil.isFixDictionaryText(attribute.getContainingFile().getText())) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        String value = attribute.getValue();
                        if (value == null) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        int startOffset = attribute.getValueElement() != null
                                ? attribute.getValueElement().getTextRange().getStartOffset() - attribute.getTextRange().getStartOffset()
                                : 0;
                        return new PsiReference[]{
                                new FixDictionaryFieldReference(attribute, new com.intellij.openapi.util.TextRange(startOffset, startOffset + value.length()))
                        };
                    }
                }
        );
    }
}
