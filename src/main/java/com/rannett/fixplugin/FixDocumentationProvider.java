package com.rannett.fixplugin;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.rannett.fixplugin.dictionary.FixDictionaryCache;
import com.rannett.fixplugin.dictionary.FixTagDictionary;
import com.rannett.fixplugin.psi.FixTypes;
import com.rannett.fixplugin.util.FixUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FixDocumentationProvider implements DocumentationProvider {

    private static final Key<String> FIX_VERSION_KEY = Key.create("FIX_VERSION_CACHE");

    @Override
    public @Nullable PsiElement getCustomDocumentationElement(
            @NotNull Editor editor,
            @NotNull PsiFile file,
            @Nullable PsiElement contextElement,
            int offs
    ) {
        if (contextElement == null) {
            return null;
        }

        var elementType = contextElement.getNode().getElementType();

        // Compare directly to FixTypes.TAG and FixTypes.VALUE
        if (elementType == FixTypes.TAG || elementType == FixTypes.VALUE) {
            return contextElement;
        }

        // Optional fallback matching
        String text = contextElement.getText();
        if (text.matches("\\d+.*") || text.matches("[A-Z]+")) {
            return contextElement;
        }

        return null;
    }

    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (element == null) {
            return null;
        }

        PsiFile file = element.getContainingFile();
        String version = getOrComputeVersion(file);
        if (version == null) {
            version = "FIX.4.2"; // Default fallback
        }

        PsiElement parent = element.getParent();

        if (parent == null) {
            return null;
        }

        PsiElement tagPsi = parent.getFirstChild();
        PsiElement valuePsi = parent.getLastChild();

        if (tagPsi == null || valuePsi == null) {
            return null;
        }

        String tagNumber = tagPsi.getText();
        String value = valuePsi.getText();

        FixTagDictionary dictionary = element.getProject().getService(FixDictionaryCache.class).getDictionary(version);
        String tagName = dictionary.getTagName(tagNumber);
        String valueName = (value != null) ? dictionary.getValueName(tagNumber, value) : null;
        String type = dictionary.getFieldType(tagNumber); // New addition: Fetch field type

        if (tagName != null) {
            StringBuilder doc = new StringBuilder("<html><body>");
            doc.append("<b>Tag ").append(tagNumber).append("</b>: ").append(tagName);
            if (type != null) {
                doc.append("<br/><b>Type</b>: ").append(type);
            }
            if (value != null) {
                doc.append("<br/><b>Value ").append(value).append("</b>");
                if (valueName != null) {
                    doc.append(": ").append(valueName);
                }
            }
            doc.append("<br/>(").append(version).append(")");
            doc.append("</body></html>");
            return doc.toString();
        }

        return null;
    }

    private String getOrComputeVersion(PsiFile file) {
        String cachedVersion = file.getUserData(FIX_VERSION_KEY);
        if (cachedVersion != null) {
            return cachedVersion;
        }

        String text = file.getText();
        String version = FixUtils.extractFixVersion(text).orElse("FIX.4.2");
        file.putUserData(FIX_VERSION_KEY, version);
        return version;
    }


}
