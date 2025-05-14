// This is a generated file. Not intended for manual editing.
package com.rannett.fixplugin.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.rannett.fixplugin.psi.FixField;
import com.rannett.fixplugin.psi.FixVisitor;
import org.jetbrains.annotations.NotNull;

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

  @Override
  public String getTag() {
    return FixPsiImplUtil.getTag(this);
  }

  @Override
  public String getValue() {
    return FixPsiImplUtil.getValue(this);
  }

}
