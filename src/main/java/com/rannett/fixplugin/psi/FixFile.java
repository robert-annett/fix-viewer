// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.rannett.fixplugin.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.rannett.fixplugin.FixFileType;
import com.rannett.fixplugin.FixLanguage;
import org.jetbrains.annotations.NotNull;

public class FixFile extends PsiFileBase {

  public FixFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, FixLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return FixFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "Fix File";
  }

}
