package com.github.albanseurat.lcaplugin.language

import com.github.albanseurat.lcaplugin.language.parser.LcaParserDefinition
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test

class LcaParserTest : ParsingTestCase("", "lca", LcaParserDefinition()) {

    @Test
    fun testSimpleDataset() {
        parseFile("simple dataset", """
            dataset "elecricity" { 
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
              LcaDatasetDefinitionImpl(DATASET_DEFINITION)
                PsiElement(LcaTokenType.dataset)('dataset')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.IDENTIFIER)('elecricity')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.{)('{')
                PsiWhiteSpace(' \n        ')
                LcaDatasetBodyImpl(DATASET_BODY)
                  LcaProductsImpl(PRODUCTS)
                    PsiElement(LcaTokenType.products)('products')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.{)('{')
                    PsiWhiteSpace('\n            ')
                    LcaExchangesImpl(EXCHANGES)
                      LcaExchangeImpl(EXCHANGE)
                        PsiElement(LcaTokenType.-)('-')
                        PsiWhiteSpace(' ')
                        PsiElement(LcaTokenType.IDENTIFIER)('nuclear')
                        PsiWhiteSpace(' ')
                        PsiElement(LcaTokenType.NUMBER)('1.3e10')
                        PsiWhiteSpace(' ')
                        PsiElement(LcaTokenType.IDENTIFIER)('kBq')
                      PsiWhiteSpace('\n            ')
                      LcaExchangeImpl(EXCHANGE)
                        PsiElement(LcaTokenType.-)('-')
                        PsiWhiteSpace(' ')
                        PsiElement(LcaTokenType.IDENTIFIER)('power')
                        PsiWhiteSpace(' ')
                        PsiElement(LcaTokenType.NUMBER)('10')
                        PsiWhiteSpace(' ')
                        PsiElement(LcaTokenType.IDENTIFIER)('kg')
                      PsiWhiteSpace('\n            ')
                      LcaExchangeImpl(EXCHANGE)
                        PsiElement(LcaTokenType.-)('-')
                        PsiWhiteSpace(' ')
                        PsiElement(LcaTokenType.IDENTIFIER)('plop')
                        PsiWhiteSpace('  ')
                        PsiElement(LcaTokenType.NUMBER)('1.3')
                        PsiWhiteSpace(' ')
                        PsiElement(LcaTokenType.IDENTIFIER)('ha')
                    PsiWhiteSpace('\n        ')
                    PsiElement(LcaTokenType.})('}')
                PsiWhiteSpace('\n    ')
                PsiElement(LcaTokenType.})('}')

        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }

    @Test
    fun testEmptyDataset()
    {
        parseFile("empty dataset", """
            dataset "empty" {
            }
        """.trimIndent())

        assertEquals("""
            Lca File
              LcaDatasetDefinitionImpl(DATASET_DEFINITION)
                PsiElement(LcaTokenType.dataset)('dataset')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.IDENTIFIER)('empty')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.{)('{')
                PsiWhiteSpace('\n')
                LcaDatasetBodyImpl(DATASET_BODY)
                  <empty list>
                PsiElement(LcaTokenType.})('}')

        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }

    @Test
    fun testMultipleDatasets()
    {
        parseFile("multiple dataset", """
            dataset "first" {
                resources {
                    - "co2" 1 kg
                }
            }
            
            dataset "second" {
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
              LcaDatasetDefinitionImpl(DATASET_DEFINITION)
                PsiElement(LcaTokenType.dataset)('dataset')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.IDENTIFIER)('first')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.left-bracket)('{')
                PsiWhiteSpace('\n    ')
                LcaDatasetBodyImpl(DATASET_BODY)
                  LcaResourcesImpl(RESOURCES)
                    PsiElement(LcaTokenType.resources)('resources')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.left-bracket)('{')
                    PsiWhiteSpace('\n        ')
                    LcaBioExchangeImpl(BIO_EXCHANGE)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.IDENTIFIER)('co2')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.NUMBER)('1')
                      PsiWhiteSpace(' ')
                      LcaQuantityImpl(QUANTITY)
                        PsiElement(LcaTokenType.IDENTIFIER)('kg')
                    PsiWhiteSpace('\n    ')
                    PsiElement(LcaTokenType.right-bracker)('}')
                PsiWhiteSpace('\n')
                PsiElement(LcaTokenType.right-bracker)('}')
              PsiWhiteSpace('\n\n')
              LcaDatasetDefinitionImpl(DATASET_DEFINITION)
                PsiElement(LcaTokenType.dataset)('dataset')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.IDENTIFIER)('second')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.left-bracket)('{')
                PsiWhiteSpace('\n    ')
                LcaDatasetBodyImpl(DATASET_BODY)
                  LcaProductsImpl(PRODUCTS)
                    PsiElement(LcaTokenType.products)('products')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.left-bracket)('{')
                    PsiWhiteSpace('\n        ')
                    LcaProductExchangeImpl(PRODUCT_EXCHANGE)
                      PsiElement(LcaTokenType.list)('-')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.IDENTIFIER)('exchange')
                      PsiWhiteSpace(' ')
                      PsiElement(LcaTokenType.NUMBER)('1')
                      PsiWhiteSpace(' ')
                      LcaQuantityImpl(QUANTITY)
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
            dataset quantity {
                inputs {
                    - wheat 1 kg
                    - land 2 m2
                    - "carbon dioxyde" 2 kg
                }
               
            }
        """.trimIndent())

        assertEquals("""
        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }


    @Test
    fun testFaultyUnitSyntax() {
        parseFile("empty dataset", """
            dataset faulty {
                inputs {
                    - wheat 1 kwh
                }
            }
        """.trimIndent())

        assertEquals("""
        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }


    @Test
    fun testMetaProperties() {
        parseFile("meta properties", """
            dataset props {
                meta {
                    - test: "property value"
                }
            }
        """.trimIndent())

        assertEquals("""
            Lca File
              LcaDatasetDefinitionImpl(DATASET_DEFINITION)
                PsiElement(LcaTokenType.dataset)('dataset')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.IDENTIFIER)('props')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.left-bracket)('{')
                PsiWhiteSpace('\n    ')
                LcaDatasetBodyImpl(DATASET_BODY)
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
                      PsiElement(LcaTokenType.lstring)('"')
                      PsiElement(LcaTokenType.STRING)('property value')
                      PsiElement(LcaTokenType.rstring)('"')
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
            dataset props {
                meta {
                    - test: "property \"value\""
                }
            }
        """.trimIndent())

        assertEquals("""
        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }


    @Test
    fun testSubstances() {
        parseFile("substances", """
            dataset "substances" {
                resources {
                    - "carbon" 1 kg
                }
            }
        """.trimIndent())

        assertEquals("""
        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }


    override fun getTestDataPath(): String {
        return ""
    }
}