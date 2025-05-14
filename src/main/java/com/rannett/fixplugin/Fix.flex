// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.rannett.fixplugin;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.rannett.fixplugin.psi.FixTypes;
import com.intellij.psi.TokenType;

%%

%class FixLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

CRLF=[\R]
WHITE_SPACE=[\ \n\t\f]
FIRST_VALUE_CHARACTER=[^ \n\f\\] | "\\"{CRLF} | "\\".
VALUE_CHARACTER=[^\|\n\f\\\u0001] | "\\"{CRLF} | "\\".
END_OF_LINE_COMMENT=("#"|"!")[^\r\n]*
SEPARATOR=[=]
TAG_CHARACTER=[\d]
FIELD_SEPARATOR=[\|\u0001]

%state WAITING_VALUE

%%
<YYINITIAL> {END_OF_LINE_COMMENT}                           {
    yybegin(YYINITIAL);
    return FixTypes.TAG;
      }
<YYINITIAL> {FIELD_SEPARATOR}                               { yybegin(YYINITIAL); return FixTypes.FIELD_SEPARATOR; }
<YYINITIAL> {TAG_CHARACTER}+                                { yybegin(YYINITIAL); return FixTypes.TAG; }
<YYINITIAL> {SEPARATOR}                                     { yybegin(WAITING_VALUE); return FixTypes.SEPARATOR; }
<WAITING_VALUE> {CRLF}({CRLF}|{WHITE_SPACE})+               { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
<WAITING_VALUE> {WHITE_SPACE}+                              { yybegin(WAITING_VALUE); return TokenType.WHITE_SPACE; }
<WAITING_VALUE> {FIRST_VALUE_CHARACTER}{VALUE_CHARACTER}*   { yybegin(YYINITIAL); return FixTypes.VALUE; }

({CRLF}|{WHITE_SPACE})+                                     { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }

[^]                                                         { return TokenType.BAD_CHARACTER; }
