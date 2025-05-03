package com.rannett.fixplugin.psi;

import com.intellij.psi.tree.IElementType;
import com.rannett.fixplugin.FixLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class FixTokenType extends IElementType {

    public FixTokenType(@NotNull @NonNls String debugName) {
        super(debugName, FixLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "FixTokenType." + super.toString();
    }
}
