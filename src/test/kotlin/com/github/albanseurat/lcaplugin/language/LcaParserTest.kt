package com.github.albanseurat.lcaplugin.language

import com.github.albanseurat.lcaplugin.language.parser.LcaParserDefinition
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test

class LcaParserTest : ParsingTestCase("", "lca", LcaParserDefinition()) {

    @Test
    fun testSimpleDataset() {
        parseFile("simple dataset", """
            dataset elecricity { 
                    products {
                        - nuclear 1.3e10 kBq
                        - power 10 kg
                        - plop  1.3 ha
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
            dataset empty {
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
            dataset first {
                resources {
                    - co2 1 kg
                }
            }
            
            dataset second {
                products {
                    - exchange 1 kg
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
                PsiElement(LcaTokenType.{)('{')
                PsiWhiteSpace('\n\n')
                LcaDatasetBodyImpl(DATASET_BODY)
                  <empty list>
                PsiElement(LcaTokenType.})('}')
              PsiWhiteSpace('\n\n')
              LcaDatasetDefinitionImpl(DATASET_DEFINITION)
                PsiElement(LcaTokenType.dataset)('dataset')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.IDENTIFIER)('second')
                PsiWhiteSpace(' ')
                PsiElement(LcaTokenType.{)('{')
                PsiWhiteSpace('\n    ')
                LcaDatasetBodyImpl(DATASET_BODY)
                  LcaProductsImpl(PRODUCTS)
                    PsiElement(LcaTokenType.products)('products')
                    PsiWhiteSpace(' ')
                    PsiElement(LcaTokenType.{)('{')
                    PsiWhiteSpace('\n        ')
                    LcaExchangesImpl(EXCHANGES)
                      LcaExchangeImpl(EXCHANGE)
                        PsiElement(LcaTokenType.-)('-')
                        PsiWhiteSpace(' ')
                        PsiElement(LcaTokenType.IDENTIFIER)('exchange')
                        PsiWhiteSpace(' ')
                        PsiElement(LcaTokenType.NUMBER)('1')
                        PsiWhiteSpace(' ')
                        PsiElement(LcaTokenType.IDENTIFIER)('kg')
                    PsiWhiteSpace('\n    ')
                    PsiElement(LcaTokenType.})('}')
                PsiWhiteSpace('\n')
                PsiElement(LcaTokenType.})('}')
            
        """.trimIndent(),
            toParseTreeText(myFile, skipSpaces(), includeRanges()))
    }

    @Test
    fun testFaultyQuantity() {
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
                    test = "property value"
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