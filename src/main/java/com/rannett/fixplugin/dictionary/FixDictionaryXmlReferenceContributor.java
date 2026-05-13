package com.rannett.fixplugin.dictionary;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class FixDictionaryXmlReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(XmlAttributeValue.class),
                new com.intellij.psi.PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull com.intellij.psi.PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        XmlAttributeValue attributeValue = (XmlAttributeValue) element;
                        if (!(attributeValue.getParent() instanceof XmlAttribute attribute)) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        if (!"name".equals(attribute.getName())) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        if (attribute.getParent() == null) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        com.intellij.psi.xml.XmlTag referenceTag = attribute.getParent().getParentTag();
                        if (referenceTag == null || !"field".equals(referenceTag.getName())) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        com.intellij.psi.xml.XmlTag containerTag = referenceTag.getParentTag();
                        if (containerTag == null || !"message".equals(containerTag.getName())) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        if (!FixDictionaryXmlUtil.isFixDictionaryText(attribute.getContainingFile().getText())) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        String value = attribute.getValue();
                        if (value == null) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        return new PsiReference[]{
                                new FixDictionaryFieldReference(attributeValue, TextRange.from(1, value.length()))
                        };
                    }
                }
        );
    }
}
