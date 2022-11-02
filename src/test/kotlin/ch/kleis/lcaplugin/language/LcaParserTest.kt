package ch.kleis.lcaplugin.language

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test

class LcaParserTest : ParsingTestCase("", "lca", LcaParserDefinition()) {

    @Test
    fun testSimpleprocess() {
        parseFile("simple process", """
            process "elecricity" { 
                    products {
                        - "electricity" 1.3 MJ
                        - "water" 1 l
                    }
                    inputs {
                        - "uranium" 1 kg
                    }
                }
        """.trimIndent())
        assertEquals("""
            Lca File
              LcaProcessDefinitionImpl(PROCESS_DEFINITION)
                PsiElement(LcaTokenType.process)('process')
                PsiWhiteSpace(' ')
                LcaStringLiteralImpl(STRING_LITERAL)
                  PsiElement(LcaTokenType.string)('"elecricity"')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.left-bracket)('{')
                PsiWhiteSpace(' \n        ')
                LcaProcessBodyImpl(PROCESS_BODY)
                  LcaProductsImpl(PRODUCTS)
                    PsiElement(LcaTokenType.products)('products')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.left-bracket)('{')
                    PsiWhiteSpace('\n            ')
                    Product(electricity)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      LcaStringLiteralImpl(STRING_LITERAL)
                        PsiElement(LcaTokenType.string)('"electricity"')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.NUMBER)('1.3')
                      PsiWhiteSpace(' ')
                      LcaUnitImpl(UNIT)
                        PsiElement(LcaTokenType.IDENTIFIER)('MJ')
                    PsiWhiteSpace('\n            ')
                    Product(water)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      LcaStringLiteralImpl(STRING_LITERAL)
                        PsiElement(LcaTokenType.string)('"water"')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.NUMBER)('1')
                      PsiWhiteSpace(' ')
                      LcaUnitImpl(UNIT)
                        PsiElement(LcaTokenType.IDENTIFIER)('l')
                    PsiWhiteSpace('\n        ')
                    PsiElement(LcaTokenType.right-bracker)('}')
                  PsiWhiteSpace('\n        ')
                  LcaInputsImpl(INPUTS)
                    PsiElement(LcaTokenType.inputs)('inputs')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.left-bracket)('{')
                    PsiWhiteSpace('\n            ')
                    LcaInputExchangeImpl(INPUT_EXCHANGE)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      LcaStringLiteralImpl(STRING_LITERAL)
                        PsiElement(LcaTokenType.string)('"uranium"')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.NUMBER)('1')
                      PsiWhiteSpace(' ')
                      LcaUnitImpl(UNIT)
                        PsiElement(LcaTokenType.IDENTIFIER)('kg')
                    PsiWhiteSpace('\n        ')
                    PsiElement(LcaTokenType.right-bracker)('}')
                PsiWhiteSpace('\n    ')
                PsiElement(LcaTokenType.right-bracker)('}')

        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }

    @Test
    fun testEmptyprocess()
    {
        parseFile("empty process", """
            process "empty" {
            }
        """.trimIndent())

        assertEquals("""
            Lca File
              LcaProcessDefinitionImpl(PROCESS_DEFINITION)
                PsiElement(LcaTokenType.process)('process')
                PsiWhiteSpace(' ')
                LcaStringLiteralImpl(STRING_LITERAL)
                  PsiElement(LcaTokenType.string)('"empty"')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.left-bracket)('{')
                PsiWhiteSpace('\n')
                LcaProcessBodyImpl(PROCESS_BODY)
                  <empty list>
                PsiElement(LcaTokenType.right-bracker)('}')

        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }

    @Test
    fun testMultipleprocesss()
    {
        parseFile("multiple process", """
            process "first" {
                resources {
                    - "co2" 1 kg
                }
            }
            
            process "second" {
                products {
                    - "exchange" 1 kg
                }
                inputs {
                    - "test" 1 kg
                }
            }
        """.trimIndent())

        assertEquals("""
            Lca File
              LcaProcessDefinitionImpl(PROCESS_DEFINITION)
                PsiElement(LcaTokenType.process)('process')
                PsiWhiteSpace(' ')
                LcaStringLiteralImpl(STRING_LITERAL)
                  PsiElement(LcaTokenType.string)('"first"')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.left-bracket)('{')
                PsiWhiteSpace('\n    ')
                LcaProcessBodyImpl(PROCESS_BODY)
                  LcaResourcesImpl(RESOURCES)
                    PsiElement(LcaTokenType.resources)('resources')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.left-bracket)('{')
                    PsiWhiteSpace('\n        ')
                    LcaBioExchangeImpl(BIO_EXCHANGE)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      LcaSubstanceIdImpl(SUBSTANCE_ID)
                        LcaStringLiteralImpl(STRING_LITERAL)
                          PsiElement(LcaTokenType.string)('"co2"')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.NUMBER)('1')
                      PsiWhiteSpace(' ')
                      LcaUnitImpl(UNIT)
                        PsiElement(LcaTokenType.IDENTIFIER)('kg')
                    PsiWhiteSpace('\n    ')
                    PsiElement(LcaTokenType.right-bracker)('}')
                PsiWhiteSpace('\n')
                PsiElement(LcaTokenType.right-bracker)('}')
              PsiWhiteSpace('\n\n')
              LcaProcessDefinitionImpl(PROCESS_DEFINITION)
                PsiElement(LcaTokenType.process)('process')
                PsiWhiteSpace(' ')
                LcaStringLiteralImpl(STRING_LITERAL)
                  PsiElement(LcaTokenType.string)('"second"')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.left-bracket)('{')
                PsiWhiteSpace('\n    ')
                LcaProcessBodyImpl(PROCESS_BODY)
                  LcaProductsImpl(PRODUCTS)
                    PsiElement(LcaTokenType.products)('products')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.left-bracket)('{')
                    PsiWhiteSpace('\n        ')
                    Product(exchange)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      LcaStringLiteralImpl(STRING_LITERAL)
                        PsiElement(LcaTokenType.string)('"exchange"')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.NUMBER)('1')
                      PsiWhiteSpace(' ')
                      LcaUnitImpl(UNIT)
                        PsiElement(LcaTokenType.IDENTIFIER)('kg')
                    PsiWhiteSpace('\n    ')
                    PsiElement(LcaTokenType.right-bracker)('}')
                  PsiWhiteSpace('\n    ')
                  LcaInputsImpl(INPUTS)
                    PsiElement(LcaTokenType.inputs)('inputs')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.left-bracket)('{')
                    PsiWhiteSpace('\n        ')
                    LcaInputExchangeImpl(INPUT_EXCHANGE)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      LcaStringLiteralImpl(STRING_LITERAL)
                        PsiElement(LcaTokenType.string)('"test"')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.NUMBER)('1')
                      PsiWhiteSpace(' ')
                      LcaUnitImpl(UNIT)
                        PsiElement(LcaTokenType.IDENTIFIER)('kg')
                    PsiWhiteSpace('\n    ')
                    PsiElement(LcaTokenType.right-bracker)('}')
                PsiWhiteSpace('\n')
                PsiElement(LcaTokenType.right-bracker)('}')

        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }

    @Test
    fun testQuantity() {
        parseFile("quantity", """
            process "quantity" {
                inputs {
                    - "wheat" 1 kg
                    - "land" 2 m2
                    - "carbon dioxyde" 2 kg
                }
               
            }
        """.trimIndent())

        assertEquals("""
            Lca File
              LcaProcessDefinitionImpl(PROCESS_DEFINITION)
                PsiElement(LcaTokenType.process)('process')
                PsiWhiteSpace(' ')
                LcaStringLiteralImpl(STRING_LITERAL)
                  PsiElement(LcaTokenType.string)('"quantity"')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.left-bracket)('{')
                PsiWhiteSpace('\n    ')
                LcaProcessBodyImpl(PROCESS_BODY)
                  LcaInputsImpl(INPUTS)
                    PsiElement(LcaTokenType.inputs)('inputs')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.left-bracket)('{')
                    PsiWhiteSpace('\n        ')
                    LcaInputExchangeImpl(INPUT_EXCHANGE)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      LcaStringLiteralImpl(STRING_LITERAL)
                        PsiElement(LcaTokenType.string)('"wheat"')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.NUMBER)('1')
                      PsiWhiteSpace(' ')
                      LcaUnitImpl(UNIT)
                        PsiElement(LcaTokenType.IDENTIFIER)('kg')
                    PsiWhiteSpace('\n        ')
                    LcaInputExchangeImpl(INPUT_EXCHANGE)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      LcaStringLiteralImpl(STRING_LITERAL)
                        PsiElement(LcaTokenType.string)('"land"')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.NUMBER)('2')
                      PsiWhiteSpace(' ')
                      LcaUnitImpl(UNIT)
                        PsiElement(LcaTokenType.IDENTIFIER)('m2')
                    PsiWhiteSpace('\n        ')
                    LcaInputExchangeImpl(INPUT_EXCHANGE)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      LcaStringLiteralImpl(STRING_LITERAL)
                        PsiElement(LcaTokenType.string)('"carbon dioxyde"')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.NUMBER)('2')
                      PsiWhiteSpace(' ')
                      LcaUnitImpl(UNIT)
                        PsiElement(LcaTokenType.IDENTIFIER)('kg')
                    PsiWhiteSpace('\n    ')
                    PsiElement(LcaTokenType.right-bracker)('}')
                PsiWhiteSpace('\n   \n')
                PsiElement(LcaTokenType.right-bracker)('}')

        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }


    @Test
    fun testFaultyUnitSyntax() {
        parseFile("empty process", """
            process "faulty" {
                inputs {
                    - "wheat" 1 kwh
                }
            }
        """.trimIndent())

        assertEquals("""
            Lca File
              LcaProcessDefinitionImpl(PROCESS_DEFINITION)
                PsiElement(LcaTokenType.process)('process')
                PsiWhiteSpace(' ')
                LcaStringLiteralImpl(STRING_LITERAL)
                  PsiElement(LcaTokenType.string)('"faulty"')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.left-bracket)('{')
                PsiWhiteSpace('\n    ')
                LcaProcessBodyImpl(PROCESS_BODY)
                  LcaInputsImpl(INPUTS)
                    PsiElement(LcaTokenType.inputs)('inputs')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.left-bracket)('{')
                    PsiWhiteSpace('\n        ')
                    LcaInputExchangeImpl(INPUT_EXCHANGE)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      LcaStringLiteralImpl(STRING_LITERAL)
                        PsiElement(LcaTokenType.string)('"wheat"')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.NUMBER)('1')
                      PsiWhiteSpace(' ')
                      PsiErrorElement:kwh is not a valid unit
                        PsiElement(LcaTokenType.IDENTIFIER)('kwh')
                    PsiWhiteSpace('\n    ')
                    PsiElement(LcaTokenType.right-bracker)('}')
                PsiWhiteSpace('\n')
                PsiElement(LcaTokenType.right-bracker)('}')

        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }


    @Test
    fun testMetaProperties() {
        parseFile("meta properties", """
            process "props" {
                meta {
                    - test: "property value"
                }
            }
        """.trimIndent())

        assertEquals("""
            Lca File
              LcaProcessDefinitionImpl(PROCESS_DEFINITION)
                PsiElement(LcaTokenType.process)('process')
                PsiWhiteSpace(' ')
                LcaStringLiteralImpl(STRING_LITERAL)
                  PsiElement(LcaTokenType.string)('"props"')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.left-bracket)('{')
                PsiWhiteSpace('\n    ')
                LcaProcessBodyImpl(PROCESS_BODY)
                  LcaMetadataImpl(METADATA)
                    PsiElement(LcaTokenType.meta)('meta')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.left-bracket)('{')
                    PsiWhiteSpace('\n        ')
                    LcaPropertyImpl(PROPERTY)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.IDENTIFIER)('test')
                      PsiElement(LcaTokenType.separator)(':')
                      PsiWhiteSpace(' ')
                      LcaStringLiteralImpl(STRING_LITERAL)
                        PsiElement(LcaTokenType.string)('"property value"')
                    PsiWhiteSpace('\n    ')
                    PsiElement(LcaTokenType.right-bracker)('}')
                PsiWhiteSpace('\n')
                PsiElement(LcaTokenType.right-bracker)('}')

        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }

    @Test
    fun testEscapedCharacter() {
        parseFile("meta properties", """
            process "props" {
                meta {
                    - test: "property \"value\""
                }
            }
        """.trimIndent())

        assertEquals("""
            Lca File
              LcaProcessDefinitionImpl(PROCESS_DEFINITION)
                PsiElement(LcaTokenType.process)('process')
                PsiWhiteSpace(' ')
                LcaStringLiteralImpl(STRING_LITERAL)
                  PsiElement(LcaTokenType.string)('"props"')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.left-bracket)('{')
                PsiWhiteSpace('\n    ')
                LcaProcessBodyImpl(PROCESS_BODY)
                  LcaMetadataImpl(METADATA)
                    PsiElement(LcaTokenType.meta)('meta')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.left-bracket)('{')
                    PsiWhiteSpace('\n        ')
                    LcaPropertyImpl(PROPERTY)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.IDENTIFIER)('test')
                      PsiElement(LcaTokenType.separator)(':')
                      PsiWhiteSpace(' ')
                      LcaStringLiteralImpl(STRING_LITERAL)
                        PsiElement(LcaTokenType.string)('"property \"value\""')
                    PsiWhiteSpace('\n    ')
                    PsiElement(LcaTokenType.right-bracker)('}')
                PsiWhiteSpace('\n')
                PsiElement(LcaTokenType.right-bracker)('}')

        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }


    @Test
    fun testResources() {
        parseFile("resources", """
            process "resources" {
                resources {
                    - "carbon" 1 kg
                }
            }
        """.trimIndent())

        assertEquals("""
            Lca File
              LcaProcessDefinitionImpl(PROCESS_DEFINITION)
                PsiElement(LcaTokenType.process)('process')
                PsiWhiteSpace(' ')
                LcaStringLiteralImpl(STRING_LITERAL)
                  PsiElement(LcaTokenType.string)('"resources"')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.left-bracket)('{')
                PsiWhiteSpace('\n    ')
                LcaProcessBodyImpl(PROCESS_BODY)
                  LcaResourcesImpl(RESOURCES)
                    PsiElement(LcaTokenType.resources)('resources')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.left-bracket)('{')
                    PsiWhiteSpace('\n        ')
                    LcaBioExchangeImpl(BIO_EXCHANGE)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      LcaSubstanceIdImpl(SUBSTANCE_ID)
                        LcaStringLiteralImpl(STRING_LITERAL)
                          PsiElement(LcaTokenType.string)('"carbon"')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.NUMBER)('1')
                      PsiWhiteSpace(' ')
                      LcaUnitImpl(UNIT)
                        PsiElement(LcaTokenType.IDENTIFIER)('kg')
                    PsiWhiteSpace('\n    ')
                    PsiElement(LcaTokenType.right-bracker)('}')
                PsiWhiteSpace('\n')
                PsiElement(LcaTokenType.right-bracker)('}')

        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }



    @Test
    fun testSubstances() {
        parseFile("substances", """
            substance "test","soil","low. pop." {
                type: resources
                unit: kg
            }
        """.trimIndent())

        assertEquals("""
            Lca File
              Substance(test, soil, low. pop.)
                PsiElement(LcaTokenType.substances)('substance')
                PsiWhiteSpace(' ')
                LcaSubstanceIdImpl(SUBSTANCE_ID)
                  LcaStringLiteralImpl(STRING_LITERAL)
                    PsiElement(LcaTokenType.string)('"test"')
                  PsiElement(LcaTokenType.coma)(',')
                  LcaStringLiteralImpl(STRING_LITERAL)
                    PsiElement(LcaTokenType.string)('"soil"')
                  PsiElement(LcaTokenType.coma)(',')
                  LcaStringLiteralImpl(STRING_LITERAL)
                    PsiElement(LcaTokenType.string)('"low. pop."')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.left-bracket)('{')
                PsiWhiteSpace('\n    ')
                LcaSubstanceBodyImpl(SUBSTANCE_BODY)
                  LcaSubstanceTypeImpl(SUBSTANCE_TYPE)
                    PsiElement(LcaTokenType.type)('type')
                    PsiElement(LcaTokenType.separator)(':')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.resources)('resources')
                  PsiWhiteSpace('\n    ')
                  LcaUnitTypeImpl(UNIT_TYPE)
                    PsiElement(LcaTokenType.unit)('unit')
                    PsiElement(LcaTokenType.separator)(':')
                    PsiWhiteSpace(' ')
                    LcaUnitImpl(UNIT)
                      PsiElement(LcaTokenType.IDENTIFIER)('kg')
                PsiWhiteSpace('\n')
                PsiElement(LcaTokenType.right-bracker)('}')

        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }



    override fun getTestDataPath(): String {
        return ""
    }
}
