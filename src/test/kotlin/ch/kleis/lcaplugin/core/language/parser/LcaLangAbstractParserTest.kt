package ch.kleis.lcaplugin.core.language.parser

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import junit.framework.TestCase
import org.junit.Test

class LcaLangAbstractParserTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun testParse() {
        // given
        val file = parseFile(
            "hello", """
            package hello
            
            process a {
                1 kg carrot
                
                inputs {
                    10 l water
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser {
            listOf(file)
        }

        // when
        val (pkg, _) = parser.collect("hello")
        val actual = pkg.definitions["a"]!!

        // then
        val expected = EProcess(
            listOf(
                EExchange(EQuantity(1.0, EVar("kg")), EVar("carrot")),
                EBlock(
                    listOf(
                        EExchange(ENeg(EQuantity(10.0, EVar("l"))), EVar("water")),
                    )
                )
            )
        )
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testIndicatorParse_shouldReturnAProduct() {
        // given
        val file = parseFile("climateChange","""
            package climateChange
            
            indicator climate_change {
                name = "Climate change"
                reference_unit = kg
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser {
            listOf(file)
        }
        // when
        val (pkg, _) = parser.collect("climateChange")
        val actual = pkg.definitions["climate_change"]!!
        // then
        val expected = EProduct("climate_change", EVar("kg"))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testSubstanceParse_shouldReturnAProduct() {
        // given
        val file = parseFile("substances","""
            package substances
            
            substance phosphate {
                name = "phosphate"
                compartment = "phosphate compartment"
                sub_compartment = "phosphate sub-compartment"
                reference_unit = kg
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser {
            listOf(file)
        }
        // when
        val (pkg, _) = parser.collect("substances")
        val actual = pkg.definitions["phosphate"]!!
        // then
        val expected = EProduct("phosphate", EVar("kg"))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testSubstanceParse_shouldReturnAProcess() {
        // given
        val file = parseFile("substances","""
            package substances
            
            substance phosphate {
                name = "phosphate"
                compartment = "phosphate compartment"
                sub_compartment = "phosphate sub-compartment"
                reference_unit = kg
                
                emission_factors {
                    1 kg climate_change
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser {
            listOf(file)
        }
        // when
        val (pkg, _) = parser.collect("substances")
        val actual = pkg.definitions["phosphate_process"]!!
        // then
        val expected = EProcess(listOf(
            EExchange(EQuantity(1.0, EVar("kg")), EVar("phosphate")),
            EBlock(listOf(
                    EExchange(
                        EQuantity(1.0,EVar("kg")),
                        EVar("climate_change")
                    )
                ))
            ))
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testSubstanceParse_shouldNotReturnAProcessWhenNoEmissionFactors() {
        // given
        val file = parseFile("substances","""
            package substances
            
            substance phosphate {
                name = "phosphate"
                compartment = "phosphate compartment"
                sub_compartment = "phosphate sub-compartment"
                reference_unit = kg
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser {
            listOf(file)
        }
        // when
        val (pkg, _) = parser.collect("substances")
        TestCase.assertNull(pkg.definitions["phosphate_process"])
    }

    @Test
    fun testSubstanceParse_shouldNotReturnAProcessThatProduceTheSubstance() {
        // given
        val file = parseFile("substances","""
            package substances
             
            substance phosphate {
                name = "phosphate"
                compartment = "phosphate compartment"
                sub_compartment = "phosphate sub-compartment"
                reference_unit = kg
                
                emission_factors {
                    1 kg climate_change
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser {
            listOf(file)
        }
        // when
        val (pkg, _) = parser.collect("substances")
        val actual = pkg.definitions["phosphate_process"]!!
        // then
        val expected = EProcess(listOf(
            EExchange(
                EQuantity(1.0, EVar("kg")),
                EVar("phosphate")),
            EBlock(
                listOf(
                EExchange(
                    EQuantity(1.0,EVar("kg")),
                    EVar("climate_change")
                )
            ))))
        TestCase.assertEquals(expected, actual)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
