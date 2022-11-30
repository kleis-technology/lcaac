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
Identifier     = \w+
StringContent  = \" ( [^\\\"] | \\[^] )* ( \" | \\ )?

Number_Exp = [eE][+-]?[0-9]+
Number_Int = [0-9][0-9]*

CommentContent = .*
FormulaContent = [^}]*

%state COMMENT_BLOCK, COMMENT_LINE, FORMULA
%%

<YYINITIAL> "//"                     {
                                         yybegin(COMMENT_LINE);
                                         return LcaTypes.COMMENT_LINE_START;
                                     }

<COMMENT_LINE> {CommentContent}      {
                                         yybegin(YYINITIAL);
                                         return LcaTypes.COMMENT_CONTENT;
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

<YYINITIAL> "${"                     {
                                          yybegin(FORMULA);
                                          return LcaTypes.FORMULA_START;
                                      }
<FORMULA> {FormulaContent}            {
                                          yybegin(FORMULA);
                                          return LcaTypes.FORMULA_CONTENT;
                                      }
<FORMULA> "}"                         {
                                          yybegin(YYINITIAL);
                                          return LcaTypes.FORMULA_END;
                                      }

<YYINITIAL> "process"                { return LcaTypes.PROCESS_KEYWORD; }
<YYINITIAL> "package"                { return LcaTypes.PACKAGE_KEYWORD; }
<YYINITIAL> "import"                { return LcaTypes.IMPORT_KEYWORD; }

<YYINITIAL> "parameters"             { return LcaTypes.PARAMETERS_KEYWORD; }
<YYINITIAL> "inputs"                 { return LcaTypes.INPUTS_KEYWORD; }
<YYINITIAL> "products"               { return LcaTypes.PRODUCTS_KEYWORD; }
<YYINITIAL> "resources"              { return LcaTypes.RESOURCES_KEYWORD; }
<YYINITIAL> "emissions"              { return LcaTypes.EMISSIONS_KEYWORD; }
<YYINITIAL> "land_use"               { return LcaTypes.LAND_USE_KEYWORD; }
<YYINITIAL> "meta"                   { return LcaTypes.META_KEYWORD; }
<YYINITIAL> "impact"                 { return LcaTypes.IMPACT_KEYWORD; }
<YYINITIAL> "factors"                { return LcaTypes.FACTORS_KEYWORD; }
<YYINITIAL> "substance"              { return LcaTypes.SUBSTANCE_KEYWORD; }
<YYINITIAL> "reference"              { return LcaTypes.REFERENCE_KEYWORD; }
<YYINITIAL> "type"                   { return LcaTypes.TYPE_KEYWORD; }
<YYINITIAL> "unit"                   { return LcaTypes.UNIT_KEYWORD; }
<YYINITIAL> "name"                   { return LcaTypes.NAME_KEYWORD; }
<YYINITIAL> "compartment"            { return LcaTypes.COMPARTMENT_KEYWORD; }
<YYINITIAL> "sub_compartment"        { return LcaTypes.SUB_COMPARTMENT_KEYWORD; }


<YYINITIAL> [+-]?{Number_Int} ("." {Number_Int}? )? {Number_Exp}? { return LcaTypes.NUMBER; }
<YYINITIAL> {Identifier}             { return LcaTypes.IDENTIFIER; }
<YYINITIAL> {StringContent}          { return LcaTypes.STRING; }

<YYINITIAL> ":"                      { return LcaTypes.SEPARATOR; }
<YYINITIAL> "{"                      { return LcaTypes.LBRACE; }
<YYINITIAL> "-"                      { return LcaTypes.LIST_ITEM; }
<YYINITIAL> "}"                      { return LcaTypes.RBRACE; }
<YYINITIAL> ","                      { return LcaTypes.COMA; }
<YYINITIAL> "."                      { return LcaTypes.DOT; }
<YYINITIAL> "*"                      { return LcaTypes.STAR; }


{WhiteSpace}                  { return TokenType.WHITE_SPACE; }
[^]                           { return TokenType.BAD_CHARACTER; }
