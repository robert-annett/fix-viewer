package com.rannett.fixplugin.psi;

import com.intellij.psi.tree.IElementType;
import com.rannett.fixplugin.FixLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class FixElementType  extends IElementType {

    public FixElementType(@NotNull @NonNls String debugName) {
        super(debugName, FixLanguage.INSTANCE);
    }
}
