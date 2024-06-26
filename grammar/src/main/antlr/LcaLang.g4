grammar LcaLang;

lcaFile
	:	pkg? pkgImport* ( processDefinition | dataSourceDefinition | testDefinition | unitDefinition |
	substanceDefinition | globalVariables )* EOF
	;

/*
    Package
*/

pkg
    : PACKAGE_KEYWORD urn
    ;

pkgImport
    : IMPORT_KEYWORD urn
    ;

/*
    Global variables
*/

globalVariables
    : VARIABLES_KEYWORD LBRACE globalAssignment* RBRACE
    ;
globalAssignment
    : dataRef EQUAL dataExpression
    ;

/*
    Data source
*/

dataSourceDefinition
    : DATASOURCE_KEYWORD dataSourceRef LBRACE
        (
            locationField | schema | block_meta
        )*
      RBRACE
    ;
locationField
    : LOCATION EQUAL STRING_LITERAL
    ;
schema
    : SCHEMA_KEYWORD LBRACE
        columnDefinition*
      RBRACE
    ;
columnDefinition
    : columnRef EQUAL dataExpression
    ;

dataSourceExpression
    : dataSourceRef rowFilter?
    ;
rowFilter
    : MATCH_KEYWORD  rowSelector
    | MATCH_KEYWORD  LPAREN ( rowSelector (COMMA rowSelector)* COMMA? ) RPAREN
    ;
rowSelector
    : columnRef EQUAL dataExpression
    ;

/*
    Test
*/
testDefinition
    : TEST_KEYWORD testRef LBRACE
        (block_given | block_assert | variables)*
      RBRACE
    ;
block_given : GIVEN_KEYWORD LBRACE technoInputExchange* RBRACE ;
block_assert : ASSERT_KEYWORD LBRACE rangeAssertion* RBRACE ;
rangeAssertion : uid BETWEEN_KEYWORD dataExpression AND_KEYWORD dataExpression ;

/*
    Substance
*/

substanceDefinition
    : SUBSTANCE_KEYWORD substanceRef LBRACE
        nameField
        typeField
        compartmentField
        subCompartmentField?
        referenceUnitField
        ( block_impacts | block_meta )*
      RBRACE
    ;

/*
    Fields
*/

nameField
    : NAME_KEYWORD EQUAL STRING_LITERAL
    ;

typeField
    : TYPE_KEYWORD EQUAL ( TYPE_EMISSION_KEYWORD | TYPE_RESOURCE_KEYWORD | TYPE_LAND_USE_KEYWORD)
    ;

compartmentField
    : COMPARTMENT_KEYWORD EQUAL STRING_LITERAL
    ;

subCompartmentField
    : SUB_COMPARTMENT_KEYWORD EQUAL STRING_LITERAL
    ;

dimField
    : DIMENSION_KEYWORD EQUAL STRING_LITERAL
    ;

referenceUnitField
    : REFERENCE_UNIT_KEYWORD EQUAL dataExpression
    ;

symbolField
    : SYMBOL_KEYWORD EQUAL STRING_LITERAL
    ;

aliasForField
    : ALIAS_FOR_KEYWORD EQUAL dataExpression
    ;

/*
    Meta
*/

block_meta
    : META_KEYWORD LBRACE meta_assignment* RBRACE
    ;
meta_assignment
    : STRING_LITERAL EQUAL STRING_LITERAL
    ;

/*
    Process
*/

processDefinition
    : PROCESS_KEYWORD name=processRef LBRACE
        (
            params
            | labels
            | variables
            | block_products
            | block_inputs
            | block_emissions
            | block_land_use
            | block_resources
            | block_meta
            | block_impacts
        )* RBRACE

    ;

labels
    : LABELS_KEYWORD LBRACE label_assignment* RBRACE
    ;
label_assignment
    : labelRef EQUAL STRING_LITERAL
    ;

params
    : PARAMETERS_KEYWORD LBRACE assignment* RBRACE
    ;

variables
    : VARIABLES_KEYWORD LBRACE assignment* RBRACE
    ;

assignment
    : dataRef sep=EQUAL dataExpression
    ;

/*
    Blocks
*/

block_products
    : PRODUCTS_KEYWORD LBRACE technoProductExchange* RBRACE
    ;

block_inputs
    : INPUTS_KEYWORD LBRACE technoInputExchange* RBRACE
    ;

block_emissions
    : EMISSIONS_KEYWORD LBRACE bioExchange* RBRACE
    ;

block_land_use
    : LAND_USE_KEYWORD LBRACE bioExchange* RBRACE
    ;

block_resources
    : RESOURCES_KEYWORD LBRACE bioExchange* RBRACE
    ;

block_impacts
    : IMPACTS_KEYWORD LBRACE impactExchange* RBRACE
    ;

/*
    Exchanges
*/

technoInputExchange
    : quantity=dataExpression product=inputProductSpec                                                             # technoEntry
    | FOR_EACH_KEYWORD dataRef FROM_KEYWORD dataSourceExpression LBRACE (variables | technoInputExchange)* RBRACE  # technoBlockForEach
    ;
technoProductExchange
    : quantity=dataExpression product=outputProductSpec
    ;
bioExchange
    : quantity=dataExpression substance=substanceSpec                                                      # bioEntry
    | FOR_EACH_KEYWORD dataRef FROM_KEYWORD dataSourceExpression LBRACE (variables | bioExchange)* RBRACE  # bioBlockForEach
    ;
impactExchange
    : quantity=dataExpression indicator=indicatorRef                                    # impactEntry
    | FOR_EACH_KEYWORD dataRef FROM_KEYWORD dataSourceExpression LBRACE (variables | impactExchange)* RBRACE   # impactBlockForEach
    ;


/*
    Quantity
*/

dataExpression
    : base=dataExpression HAT exponent=NUMBER                       # exponentialQuantityExpression
    | left=dataExpression op=(STAR | SLASH) right=dataExpression    # mulGroup
    | scale=NUMBER base=dataExpression                              # mulGroup
    | left=dataExpression op=(PLUS | MINUS) right=dataExpression    # addGroup
    | parenExpression                                               # baseGroup
    | stringExpression                                              # baseGroup
    | dataRef slice?                                                # baseGroup
    | op=LOOKUP dataSourceExpression                                # recordGroup
    | op=DEFAULT_RECORD FROM_KEYWORD dataSourceExpression           # recordGroup
    | op=SUM LPAREN dataSourceExpression COMMA columnRef (STAR columnRef)* RPAREN    # colGroup
    ;
slice
    : DOT columnRef
    ;

parenExpression
    : LPAREN dataExpression RPAREN
    ;

stringExpression
    : STRING_LITERAL
    ;

/*
    Unit
*/


unitDefinition
    : UNIT_KEYWORD dataRef LBRACE
        symbolField (dimField | aliasForField)
      RBRACE
    ;

/*
    Reference
*/

labelRef : uid ;
dataRef : uid ;
productRef : uid ;
processRef : uid ;
dataSourceRef : uid ;
substanceRef : uid ;
indicatorRef : uid ;
parameterRef : uid ;
testRef : uid ;
columnRef : uid ;


/*
    Spec
*/

outputProductSpec
    : productRef allocateField?
    ;
allocateField
    : ALLOCATE_KEYWORD dataExpression
    ;

inputProductSpec
    : productRef processTemplateSpec?
    ;
processTemplateSpec
    : FROM_KEYWORD processRef (LPAREN comma_sep_arguments? RPAREN)? matchLabels?
    ;
matchLabels
    : MATCH_KEYWORD LPAREN comma_sep_label_selectors? RPAREN
    ;
comma_sep_label_selectors
    : labelSelector (COMMA labelSelector)* COMMA?
    ;
labelSelector
    : labelRef EQUAL dataExpression
    ;
comma_sep_arguments
    : argument (COMMA argument)* COMMA?
    ;
argument
    : parameterRef EQUAL dataExpression
    ;


substanceSpec
    : substanceRef (LPAREN compartmentField (COMMA subCompartmentField)? RPAREN)?
    ;


/*
    Identifier
*/

urn : uid DOT urn | uid ;
uid : ID ;


/*
    Lexems
*/

PACKAGE_KEYWORD : 'package' ;
IMPORT_KEYWORD : 'import' ;
VARIABLES_KEYWORD : 'variables' ;
PROCESS_KEYWORD : 'process' ;
SUBSTANCE_KEYWORD : 'substance' ;
TYPE_KEYWORD : 'type' ;
TYPE_EMISSION_KEYWORD : 'Emission' ;
TYPE_RESOURCE_KEYWORD : 'Resource' ;
TYPE_LAND_USE_KEYWORD : 'Land_use' ;
COMPARTMENT_KEYWORD : 'compartment' ;
SUB_COMPARTMENT_KEYWORD : 'sub_compartment' ;
IMPACTS_KEYWORD : 'impacts' ;
META_KEYWORD : 'meta' ;
FROM_KEYWORD : 'from' ;
NAME_KEYWORD : 'name' ;
UNIT_KEYWORD : 'unit' ;
REFERENCE_UNIT_KEYWORD : 'reference_unit' ;
SYMBOL_KEYWORD : 'symbol' ;
ALIAS_FOR_KEYWORD : 'alias_for' ;
ALLOCATE_KEYWORD : 'allocate' ;
DIMENSION_KEYWORD : 'dimension' ;
PARAMETERS_KEYWORD : 'params' ;
PRODUCTS_KEYWORD : 'products' ;
INPUTS_KEYWORD  : 'inputs' ;
EMISSIONS_KEYWORD : 'emissions' ;
LAND_USE_KEYWORD : 'land_use' ;
RESOURCES_KEYWORD : 'resources' ;
MATCH_KEYWORD : 'match' ;
WHERE_KEYWORD : 'where' ;
LABELS_KEYWORD : 'labels' ;

DATASOURCE_KEYWORD : 'datasource' ;
LOCATION : 'location' ;
SCHEMA_KEYWORD : 'schema' ;

TEST_KEYWORD : 'test' ;
GIVEN_KEYWORD : 'given' ;
ASSERT_KEYWORD : 'assert' ;
BETWEEN_KEYWORD : 'between' ;
AND_KEYWORD : 'and' ;

FOR_EACH_KEYWORD : 'for_each' ;

SUM : 'sum' ;
LOOKUP : 'lookup' ;
DEFAULT_RECORD : 'default_record' ;


EQUAL : '=' ;
LBRACK : '[' ;
RBRACK : ']' ;
LBRACE : '{' ;
RBRACE : '}' ;
LPAREN : '(' ;
RPAREN : ')' ;
COMMA : ',' ;
DOT : '.' ;
PLUS : '+' ;
MINUS : '-' ;
STAR : '*' ;
SLASH : '/' ;
HAT : '^' ;
DOUBLE_QUOTE : '"' ;

LINE_COMMENT : '//' .*? ('\n'|EOF)	-> channel(HIDDEN) ;
COMMENT      : '/*' .*? '*/'    	-> channel(HIDDEN) ;

ID  : [a-zA-Z_] [a-zA-Z0-9_]* ;
NUMBER
	:   MINUS? INT '.' INT EXP?   // 1.35, 1.35E-9, 0.3, -4.5
	|   MINUS? INT EXP?            // 1e10 -3e4
	;
fragment INT : [0-9]+ ;
fragment EXP :   [Ee] [+\-]? INT ;

STRING_LITERAL :  '"' (ESC | ~["\\])* '"' ;
STRING_LITERAL_BACK_QUOTE : '`' (ESC | ~["\\])* '`' ;

fragment ESC :   '\\' ["\bfnrt] ;

WS : [ \t\n\r]+ -> channel(HIDDEN) ;

/** "catch all" rule for any char not matche in a token rule of your
 *  grammar. Lexers in Intellij must return all tokens good and bad.
 *  There must be a token to cover all characters, which makes sense, for
 *  an IDE. The parser however should not see these bad tokens because
 *  it just confuses the issue. Hence, the hidden channel.
 */
ERRCHAR
	:	.	-> channel(HIDDEN)
	;

