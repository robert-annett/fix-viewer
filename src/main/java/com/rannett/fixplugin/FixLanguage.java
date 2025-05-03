package com.rannett.fixplugin;

import com.intellij.lang.Language;

public class FixLanguage extends Language {

    public static final FixLanguage INSTANCE = new FixLanguage();

  private FixLanguage() {
        super("Fix");
    }

}