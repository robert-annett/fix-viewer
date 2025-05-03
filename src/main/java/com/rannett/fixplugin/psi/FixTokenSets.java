// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.rannett.fixplugin.psi;

import com.intellij.psi.tree.TokenSet;

public interface FixTokenSets {

  TokenSet IDENTIFIERS = TokenSet.create(FixTypes.KEY);

  TokenSet COMMENTS = TokenSet.create(FixTypes.COMMENT);

}
