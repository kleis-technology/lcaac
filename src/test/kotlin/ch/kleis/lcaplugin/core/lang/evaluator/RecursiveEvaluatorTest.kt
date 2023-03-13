package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.ProcessValue
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.TechnoExchangeValue
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.ProductValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.TemplateFixture
import org.junit.Assert.assertEquals
import org.junit.Test

class RecursiveEvaluatorTest {
    @Test
    fun eval_whenExistsFromProcessRef_thenCorrectSystem() {
        // given
        val symbolTable = SymbolTable(
            processTemplates = Register(
                mapOf(
                    "carrot_production" to TemplateFixture.carrotProduction
                )
            )
        )
        val expression = EProcessTemplate(
            emptyMap(),
            emptyMap(),
            EProcess(
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, EProductRef("salad"))
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(
                            EProductRef("carrot"),
                            FromProcessRef(
                                ETemplateRef("carrot_production"),
                                mapOf("q_water" to QuantityFixture.twoLitres),
                            )
                        )
                    )
                ),
                biosphere = emptyList()
            )
        )
        val recursiveEvaluator = RecursiveEvaluator(symbolTable)

        // when
        val actual = recursiveEvaluator.eval(expression).processes.toSet()

        // then
        val expected = setOf(
            ProcessValue(
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot
                    )
                ),
                inputs = listOf(TechnoExchangeValue(QuantityValueFixture.twoLitres, ProductValueFixture.water)),
                biosphere = emptyList(),
            ),
            ProcessValue(
                products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, ProductValueFixture.salad)),
                inputs = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, ProductValueFixture.carrot)),
                biosphere = emptyList(),
            ),
        )
        assertEquals(expected, actual)
    }
}
