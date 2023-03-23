package ch.kleis.lcaplugin.language.e2e

import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.lang.evaluator.RecursiveEvaluator
import ch.kleis.lcaplugin.core.lang.fixture.DimensionFixture
import ch.kleis.lcaplugin.core.matrix.InventoryError
import ch.kleis.lcaplugin.core.matrix.InventoryMatrix
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import junit.framework.TestCase
import org.junit.Test

class E2ETest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun test_operationPriority() {
        // given
        val file = parseFile(
            "hello", """
            process p {
                variables {
                    q = 2 m/kg
                }
                products {
                    1 kg out
                }
                inputs {
                    3 kg * q in
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(listOf(file))

        // when
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["p"]!!
        val system = RecursiveEvaluator(symbolTable).eval(entryPoint)
        val assessment = Assessment(system)
        when (val result = assessment.inventory()) {
            // then
            is InventoryError -> fail("$result")
            is InventoryMatrix -> {
                val output = result.observablePorts.getElements().first()
                val input = result.controllablePorts.getElements().first()
                val cf = result.value(output, input)

                TestCase.assertEquals("out", output.name())
                TestCase.assertEquals(1.0, cf.output.quantity().amount)
                TestCase.assertEquals(DimensionFixture.mass.getDefaultUnitValue(), cf.output.quantity().unit)

                TestCase.assertEquals("in", input.name())
                TestCase.assertEquals(6.0, cf.input.quantity().amount)
                TestCase.assertEquals(DimensionFixture.length.getDefaultUnitValue(), cf.input.quantity().unit)
            }
        }
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
