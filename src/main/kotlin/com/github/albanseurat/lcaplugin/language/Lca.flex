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
StringContent  = \" ( [^\\\"] | \\[^] )* ( \" | \\ )?

Number_Exp = [eE][+-]?[0-9]+
Number_Int = [0-9][0-9]*

%%


<YYINITIAL> "dataset"                { return LcaTypes.DATASET_KEYWORD; }

<YYINITIAL> "inputs"                 { return LcaTypes.INPUTS_KEYWORD; }
<YYINITIAL> "products"               { return LcaTypes.PRODUCTS_KEYWORD; }
<YYINITIAL> "resources"              { return LcaTypes.RESOURCES_KEYWORD; }
<YYINITIAL> "emissions"              { return LcaTypes.EMISSIONS_KEYWORD; }
<YYINITIAL> "land_use"               { return LcaTypes.LAND_USE_KEYWORD; }
<YYINITIAL> "meta"                   { return LcaTypes.META_KEYWORD; }
<YYINITIAL> "substance"              { return LcaTypes.SUBSTANCE_KEYWORD; }
<YYINITIAL> "type"                   { return LcaTypes.TYPE_KEYWORD; }
<YYINITIAL> "compartment"            { return LcaTypes.COMPARTMENT_KEYWORD; }
<YYINITIAL> "subCompartment"         { return LcaTypes.SUB_COMPARTMENT_KEYWORD; }
<YYINITIAL> "unit"                   { return LcaTypes.UNIT_KEYWORD; }


<YYINITIAL> ":"                      { return LcaTypes.SEPARATOR; }
<YYINITIAL> "{"                      { return LcaTypes.LBRACE; }
<YYINITIAL> "-"                      { return LcaTypes.LIST_ITEM; }
<YYINITIAL> "}"                      { return LcaTypes.RBRACE; }


<YYINITIAL> {Number_Int} ("." {Number_Int}? )? {Number_Exp}? { return LcaTypes.NUMBER; }
<YYINITIAL> {Identifier}             { return LcaTypes.IDENTIFIER; }
<YYINITIAL> {StringContent}          { return LcaTypes.STRING; }


{WhiteSpace}                  { return TokenType.WHITE_SPACE; }
[^]                           { return TokenType.BAD_CHARACTER; }