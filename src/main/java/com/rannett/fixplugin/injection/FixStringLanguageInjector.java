package com.rannett.fixplugin.injection;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.rannett.fixplugin.FixLanguage;
import com.rannett.fixplugin.util.FixUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Injects the Fix language into string literals when they appear to contain
 * FIX messages. This enables syntax highlighting and inspections for messages
 * embedded directly in source code.
 */
public class FixStringLanguageInjector implements MultiHostInjector {

    @Override
    /**
     * Inject the Fix language into a string literal if the text resembles a FIX message.
     */
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (!(context instanceof PsiLanguageInjectionHost host) || !host.isValidHost()) {
            return;
        }

        String text = host.getText();
        if (text == null || text.length() < 5) {
            return;
        }

        // Strip common quoting characters
        char first = text.charAt(0);
        char last = text.charAt(text.length() - 1);
        if ((first == '"' || first == '\'') && first == last && text.length() > 1) {
            text = text.substring(1, text.length() - 1);
        }

        if (FixUtils.extractFixVersion(text).isEmpty()) {
            return;
        }

        int startOffset = 0;
        int endOffset = host.getTextLength();
        if (host.getTextLength() > 1 && (first == '"' || first == '\'')) {
            startOffset = 1;
            endOffset = host.getTextLength() - 1;
        }

        registrar.startInjecting(FixLanguage.INSTANCE);
        registrar.addPlace(null, null, host, new TextRange(startOffset, endOffset));
        registrar.doneInjecting();
    }

    @Override
    /**
     * Specify that all {@link PsiLanguageInjectionHost} elements may be processed
     * for potential injection.
     */
    public @NotNull List<Class<? extends PsiElement>> elementsToInjectIn() {
        return List.of(PsiLanguageInjectionHost.class);
    }
}

