package ch.kleis.lcaplugin.language;

import ch.kleis.lcaplugin.psi.LcaTokenType;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import ch.kleis.lcaplugin.psi.LcaTypes;
import com.intellij.psi.TokenType;

%%

%public
%class LcaLexer
%implements FlexLexer
%{
    int commentDepth = 0;
%}
%unicode
%function advance
%type IElementType
%eof{return;
%eof}


WhiteSpace     = \s+
Identifier     = [a-zA-Z_]\w*
StringLiteral  = \" ( [^\\\"] | \\[^] )* ( \" | \\ )?

Number_Exp = [eE][+-]?[0-9]+
Number_Int = [0-9][0-9]*

CommentContent = .*

%state COMMENT_BLOCK
%%

<YYINITIAL> "//"{CommentContent}    {
                                         yybegin(YYINITIAL);
                                         return LcaTypes.COMMENT_LINE;
                                     }

<YYINITIAL> "/*"                     {
                                         commentDepth = 0;
                                         yybegin(COMMENT_BLOCK);
                                         commentDepth++;
                                         return LcaTypes.COMMENT_BLOCK_START;
                                     }

<COMMENT_BLOCK> "/*"                 {
                                         commentDepth++;
                                         return LcaTypes.COMMENT_CONTENT;
                                     }

<COMMENT_BLOCK> "*/"                 {
                                         commentDepth--;
                                         if (commentDepth == 0) {
                                             yybegin(YYINITIAL);
                                             return LcaTypes.COMMENT_BLOCK_END;
                                         }
                                         return LcaTypes.COMMENT_CONTENT;
                                     }

<COMMENT_BLOCK> {CommentContent}     {
                                         return LcaTypes.COMMENT_CONTENT;
                                     }

<YYINITIAL> "package"                { return LcaTypes.PACKAGE_KEYWORD; }
<YYINITIAL> "import"                { return LcaTypes.IMPORT_KEYWORD; }
<YYINITIAL> "variables"                { return LcaTypes.VARIABLES_KEYWORD; }
<YYINITIAL> "process"                { return LcaTypes.PROCESS_KEYWORD; }
<YYINITIAL> "indicator"                { return LcaTypes.INDICATOR_KEYWORD; }
<YYINITIAL> "substance"                { return LcaTypes.SUBSTANCE_KEYWORD; }
<YYINITIAL> "compartment"                { return LcaTypes.COMPARTMENT_KEYWORD; }
<YYINITIAL> "type"                { return LcaTypes.TYPE_KEYWORD; }
<YYINITIAL> "Emission"             { return LcaTypes.TYPE_EMISSION_KEYWORD; }
<YYINITIAL> "Resource"             { return LcaTypes.TYPE_RESOURCE_KEYWORD; }
<YYINITIAL> "Land_use"             { return LcaTypes.TYPE_LAND_USE_KEYWORD; }
<YYINITIAL> "sub_compartment"                { return LcaTypes.SUB_COMPARTMENT_KEYWORD; }
<YYINITIAL> "impacts"                { return LcaTypes.IMPACTS_KEYWORD; }
<YYINITIAL> "meta"                { return LcaTypes.META_KEYWORD; }
<YYINITIAL> "from"                { return LcaTypes.FROM_KEYWORD; }
<YYINITIAL> "name"                { return LcaTypes.NAME_KEYWORD; }
<YYINITIAL> "unit"                   { return LcaTypes.UNIT_KEYWORD; }
<YYINITIAL> "reference_unit"                   { return LcaTypes.REFERENCE_UNIT_KEYWORD; }
<YYINITIAL> "symbol"                   { return LcaTypes.SYMBOL_KEYWORD; }
<YYINITIAL> "alias_for"                   { return LcaTypes.ALIAS_FOR_KEYWORD; }
<YYINITIAL> "allocate"                   { return LcaTypes.ALLOCATE_KEYWORD; }
<YYINITIAL> "dimension"                   { return LcaTypes.DIMENSION_KEYWORD; }
<YYINITIAL> "params"             { return LcaTypes.PARAMETERS_KEYWORD; }
<YYINITIAL> "products"             { return LcaTypes.PRODUCTS_KEYWORD; }
<YYINITIAL> "inputs"             { return LcaTypes.INPUTS_KEYWORD; }
<YYINITIAL> "emissions"             { return LcaTypes.EMISSIONS_KEYWORD; }
<YYINITIAL> "land_use"             { return LcaTypes.LAND_USE_KEYWORD; }
<YYINITIAL> "resources"             { return LcaTypes.RESOURCES_KEYWORD; }


<YYINITIAL> [-]?{Number_Int} ("." {Number_Int}? )? {Number_Exp}? { return LcaTypes.NUMBER; }
<YYINITIAL> {Identifier}             { return LcaTypes.IDENTIFIER; }
<YYINITIAL> {StringLiteral}          { return LcaTypes.STRING_LITERAL; }

<YYINITIAL> "="                      { return LcaTypes.EQUAL; }
<YYINITIAL> "["                      { return LcaTypes.LSQBRACE; }
<YYINITIAL> "]"                      { return LcaTypes.RSQBRACE; }
<YYINITIAL> "{"                      { return LcaTypes.LBRACE; }
<YYINITIAL> "}"                      { return LcaTypes.RBRACE; }
<YYINITIAL> "("                      { return LcaTypes.LPAREN; }
<YYINITIAL> ")"                      { return LcaTypes.RPAREN; }
<YYINITIAL> ","                      { return LcaTypes.COMMA; }
<YYINITIAL> "."                      { return LcaTypes.DOT; }
<YYINITIAL> "+"                      { return LcaTypes.PLUS; }
<YYINITIAL> "-"                      { return LcaTypes.MINUS; }
<YYINITIAL> "*"                      { return LcaTypes.STAR; }
<YYINITIAL> "/"                      { return LcaTypes.SLASH; }
<YYINITIAL> "^"                      { return LcaTypes.HAT; }
<YYINITIAL> "\""                      { return LcaTypes.DOUBLE_QUOTE; }

{WhiteSpace}                  { return TokenType.WHITE_SPACE; }
[^]                           { return TokenType.BAD_CHARACTER; }
