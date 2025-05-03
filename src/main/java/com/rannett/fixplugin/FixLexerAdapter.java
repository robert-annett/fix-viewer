package com.rannett.fixplugin;

import com.intellij.lexer.FlexAdapter;

public class FixLexerAdapter extends FlexAdapter {

    public FixLexerAdapter() {
        super(new FixLexer(null));
    }
}
