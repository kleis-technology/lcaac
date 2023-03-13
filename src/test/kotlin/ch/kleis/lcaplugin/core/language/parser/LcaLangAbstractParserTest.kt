package ch.kleis.lcaplugin.core.language.parser

import ch.kleis.lcaplugin.core.lang.expression.*
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
                products {
                    1 kg carrot
                }
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
        val actual = pkg.symbolTable.getTemplate("a")!!

        // then
        val expected = EProcessTemplate(
            params = emptyMap(),
            locals = emptyMap(),
            EProcess(
                products = listOf(
                    ETechnoExchange(
                        EQuantityLiteral(1.0, EUnitRef("kg")),
                        EProductRef("carrot")
                    ),
                ),
                inputs = listOf(
                    ETechnoExchange(
                        EQuantityLiteral(10.0, EUnitRef("l")),
                        EProductRef("water")
                    ),
                ),
                biosphere = emptyList(),
            )
        )
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testSubstanceParse_shouldReturnASubstanceCharacterization() {
        // given
        val file = parseFile(
            "substances", """
            package substances
            
            substance phosphate {
                name = "phosphate"
                compartment = "phosphate compartment"
                sub_compartment = "phosphate sub-compartment"
                reference_unit = kg
                
                impacts {
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
        val actual = pkg.symbolTable.getSubstanceCharacterization("phosphate")

        // then
        val expected = ESubstanceCharacterization(
            referenceExchange = EBioExchange(
                EQuantityLiteral(1.0, EUnitRef("kg")),
                ESubstanceRef("phosphate"),
            ),
            impacts = listOf(
                EImpact(
                    EQuantityLiteral(1.0, EUnitRef("kg")),
                    EIndicatorRef("climate_change"),
                )
            )
        )
        TestCase.assertEquals(expected, actual)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
