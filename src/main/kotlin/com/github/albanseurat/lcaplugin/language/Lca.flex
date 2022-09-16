package com.github.albanseurat.lcaplugin.language;

import com.github.albanseurat.lcaplugin.psi.LcaTokenType;import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.github.albanseurat.lcaplugin.psi.LcaTypes;
import com.intellij.psi.TokenType;

%%

%class LcaLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}


LineTerminator = \r|\n|\r\n
WhiteSpace     = \s

Identifier = \w+

%%

<YYINITIAL> "dataset"         { return LcaTypes.DATASET; }
{Identifier}                  { return LcaTypes.IDENTIFIER; }

"{"                           { return LcaTypes.LBRACE; }
"}"                           { return LcaTypes.RBRACE; }

{WhiteSpace}+                 { return TokenType.WHITE_SPACE; }
[^]                           { return TokenType.BAD_CHARACTER; }