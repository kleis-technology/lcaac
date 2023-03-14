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
    fun testParse_simpleProcess() {
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
        val parser = LcaLangAbstractParser(
            listOf(file)
        )

        // when
        val symbolTable = parser.load()
        val actual = symbolTable.getTemplate("a")!!

        // then
        val expected = EProcessTemplate(
            params = emptyMap(),
            locals = emptyMap(),
            EProcess(
                products = listOf(
                    ETechnoExchange(
                        EQuantityLiteral(1.0, EUnitRef("kg")),
                        EConstrainedProduct(EProductRef("carrot"), None),
                    ),
                ),
                inputs = listOf(
                    ETechnoExchange(
                        EQuantityLiteral(10.0, EUnitRef("l")),
                        EConstrainedProduct(EProductRef("water"), None)
                    ),
                ),
                biosphere = emptyList(),
            )
        )
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testParse_withConstrainedProduct() {
        // given
        val file = parseFile(
            "hello", """
            package hello
            
            process a {
                products {
                    1 kg carrot
                }
                inputs {
                    10 l water from water_proc(x = 3 l)
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            listOf(file)
        )

        // when
        val symbolTable = parser.load()
        val expression = symbolTable.getTemplate("a")!!
        val actual =
            TemplateExpression.eProcessTemplate.body.eProcess.inputs.getAll(expression).flatten()

        // then
        val expected = listOf(
            ETechnoExchange(
                EQuantityLiteral(10.0, EUnitRef("l")),
                EConstrainedProduct(
                    EProductRef("water"),
                    FromProcessRef(
                        ETemplateRef("water_proc"),
                        mapOf("x" to EQuantityLiteral(3.0, EUnitRef("l"))),
                    ),
                )
            ),
        )
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun testParse_substanceWithImpacts_shouldReturnASubstanceCharacterization() {
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
        val parser = LcaLangAbstractParser(
            listOf(file)
        )

        // when
        val symbolTable = parser.load()
        val actual = symbolTable.getSubstanceCharacterization("phosphate")

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
