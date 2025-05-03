// This is a generated file. Not intended for manual editing.
package com.rannett.fixplugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.rannett.fixplugin.psi.FixTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.rannett.fixplugin.psi.*;

public class FixFieldImpl extends ASTWrapperPsiElement implements FixField {

  public FixFieldImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull FixVisitor visitor) {
    visitor.visitField(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof FixVisitor) accept((FixVisitor)visitor);
    else super.accept(visitor);
  }

}
