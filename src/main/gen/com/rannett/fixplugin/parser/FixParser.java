// This is a generated file. Not intended for manual editing.
package com.rannett.fixplugin.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

import static com.rannett.fixplugin.parser.FixParserUtil.*;
import static com.rannett.fixplugin.psi.FixTypes.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class FixParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return fixFile(b, l + 1);
  }

  /* ********************************************************** */
  // (TAG? SEPARATOR VALUE?) | TAG
  public static boolean field(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field")) return false;
    if (!nextTokenIs(b, "<field>", SEPARATOR, TAG)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD, "<field>");
    r = field_0(b, l + 1);
    if (!r) r = consumeToken(b, TAG);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // TAG? SEPARATOR VALUE?
  private static boolean field_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = field_0_0(b, l + 1);
    r = r && consumeToken(b, SEPARATOR);
    r = r && field_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // TAG?
  private static boolean field_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_0_0")) return false;
    consumeToken(b, TAG);
    return true;
  }

  // VALUE?
  private static boolean field_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_0_2")) return false;
    consumeToken(b, VALUE);
    return true;
  }

  /* ********************************************************** */
  // item_*
  static boolean fixFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fixFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!item_(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "fixFile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // field|FIELD_SEPARATOR|COMMENT|CRLF
  static boolean item_(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "item_")) return false;
    boolean r;
    r = field(b, l + 1);
    if (!r) r = consumeToken(b, FIELD_SEPARATOR);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, CRLF);
    return r;
  }

}
