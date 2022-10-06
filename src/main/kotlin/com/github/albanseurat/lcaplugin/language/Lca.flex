package com.github.albanseurat.lcaplugin.language;

import com.github.albanseurat.lcaplugin.psi.LcaTokenType;import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.github.albanseurat.lcaplugin.psi.LcaTypes;
import com.intellij.psi.TokenType;

%%

%public
%class LcaLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}


WhiteSpace     = \s+
Identifier     = \w+
Unit           = \w+
StringContent  = [\w\s]*

Number_Exp = [eE][+-]?[0-9]+
Number_Int = [0-9][0-9]*


%state EXCHANGE, EXCHANGE_AMOUNT, EXCHANGE_UNIT, LITERAL_STRING

%%

<YYINITIAL> "dataset"                { return LcaTypes.DATASET_KEYWORD; }

<YYINITIAL> "inputs"                 { return LcaTypes.INPUTS_KEYWORD; }
<YYINITIAL> "products"               { return LcaTypes.PRODUCTS_KEYWORD; }
<YYINITIAL> "resources"              { return LcaTypes.RESOURCES_KEYWORD; }
<YYINITIAL> "emissions"              { return LcaTypes.EMISSIONS_KEYWORD; }
<YYINITIAL> "meta"                   { return LcaTypes.META_KEYWORD; }

<YYINITIAL> "="                      { return LcaTypes.EQUALS; }
<YYINITIAL> "{"                      { return LcaTypes.LBRACE; }
<YYINITIAL> "}"                      { return LcaTypes.RBRACE; }
"-"                                  { yybegin(EXCHANGE); return LcaTypes.LIST_ITEM; }
<YYINITIAL> \"                       { yybegin(LITERAL_STRING); return LcaTypes.LSTRING; }
<LITERAL_STRING> {StringContent}     { return LcaTypes.STRING; }
<LITERAL_STRING> \"                  { yybegin(YYINITIAL); return LcaTypes.RSTRING; }

<YYINITIAL> {Identifier}             { return LcaTypes.IDENTIFIER; }


<EXCHANGE>
{
    {Identifier}                                         { yybegin(EXCHANGE_AMOUNT); return LcaTypes.IDENTIFIER; }
    <EXCHANGE_AMOUNT> {
        {Number_Int} ("." {Number_Int}? )? {Number_Exp}? { yybegin(EXCHANGE_UNIT); return LcaTypes.NUMBER; }
    }
    <EXCHANGE_UNIT> {
        {Unit}                                     { yybegin(YYINITIAL); return LcaTypes.UNIT; }
    }
}





{WhiteSpace}                  { return TokenType.WHITE_SPACE; }
[^]                           { return TokenType.BAD_CHARACTER; }