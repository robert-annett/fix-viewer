package com.rannett.fixplugin;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.rannett.fixplugin.dictionary.FixDictionaryCache;
import com.rannett.fixplugin.dictionary.FixTagDictionary;
import com.rannett.fixplugin.psi.FixTypes;
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
            version = "FIX.4.2"; // Fallback
        }

        PsiElement parent = element.getParent();
        PsiElement tagPsi = parent.getFirstChild();
        PsiElement valuePsi = parent.getLastChild();

        String tagNumber = tagPsi.getText();
        String value = valuePsi.getText();

        if (tagNumber == null) {
            return null; // Can't generate doc without tag
        }

        FixTagDictionary dictionary = FixDictionaryCache.getDictionary(element.getProject(), version);
        String tagName = dictionary.getTagName(tagNumber);
        String valueName = (value != null) ? dictionary.getValueName(tagNumber, value) : null;

        if (tagName != null) {
            StringBuilder doc = new StringBuilder("<html><body>");
            doc.append("<b>Tag ").append(tagNumber).append("</b>: ").append(tagName);
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
        if (cachedVersion != null) return cachedVersion;

        String text = file.getText();
        String version = extractFixVersion(text);
        if (version != null) {
            file.putUserData(FIX_VERSION_KEY, version);
        }
        return version;
    }

    private @Nullable String extractFixVersion(String text) {
        int start = text.indexOf("8=FIX.");
        if (start == -1) return null;

        int endPipe = text.indexOf('|', start);
        int endSoh = text.indexOf('\u0001', start);
        int end;

        if (endPipe == -1 && endSoh == -1) {
            end = text.length();
        } else if (endPipe == -1) {
            end = endSoh;
        } else if (endSoh == -1) {
            end = endPipe;
        } else {
            end = Math.min(endPipe, endSoh);
        }

        return text.substring(start + 2, end);
    }

}
