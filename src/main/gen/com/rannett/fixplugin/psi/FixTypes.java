// This is a generated file. Not intended for manual editing.
package com.rannett.fixplugin.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.rannett.fixplugin.psi.impl.*;

public interface FixTypes {

  IElementType FIELD = new FixElementType("FIELD");

  IElementType COMMENT = new FixTokenType("COMMENT");
  IElementType CRLF = new FixTokenType("CRLF");
  IElementType FIELD_SEPARATOR = new FixTokenType("FIELD_SEPARATOR");
  IElementType SEPARATOR = new FixTokenType("SEPARATOR");
  IElementType TAG = new FixTokenType("TAG");
  IElementType VALUE = new FixTokenType("VALUE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == FIELD) {
        return new FixFieldImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
