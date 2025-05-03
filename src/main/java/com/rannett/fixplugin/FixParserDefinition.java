// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.rannett.fixplugin;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.rannett.fixplugin.parser.FixParser;
import com.rannett.fixplugin.psi.FixFile;
import com.rannett.fixplugin.psi.FixTokenSets;
import com.rannett.fixplugin.psi.FixTypes;
import org.jetbrains.annotations.NotNull;

public class FixParserDefinition implements ParserDefinition {

  public static final IFileElementType FILE = new IFileElementType(FixLanguage.INSTANCE);

  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return new FixLexerAdapter();
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens() {
    return FixTokenSets.COMMENTS;
  }

  @NotNull
  @Override
  public TokenSet getStringLiteralElements() {
    return TokenSet.EMPTY;
  }

  @NotNull
  @Override
  public PsiParser createParser(final Project project) {
    return new FixParser();
  }

  @NotNull
  @Override
  public IFileElementType getFileNodeType() {
    return FILE;
  }

  @NotNull
  @Override
  public PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new FixFile(viewProvider);
  }

  @NotNull
  @Override
  public PsiElement createElement(ASTNode node) {
    return FixTypes.Factory.createElement(node);
  }

}
