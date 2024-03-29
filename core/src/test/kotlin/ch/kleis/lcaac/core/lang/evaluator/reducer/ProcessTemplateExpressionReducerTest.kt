package ch.kleis.lcaac.core.lang.evaluator.reducer

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.fixture.ProductFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.fixture.UnitFixture
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProcessTemplateExpressionReducerTest {
    private val ops = BasicOperations
    private val sourceOps = mockk<DataSourceOperations<BasicNumber>>()

    @Test
    fun reduce_whenInstance_shouldReduce() {
        // given
        val template = EProcessTemplate(
            params = mapOf(
                Pair("q_carrot", QuantityFixture.oneKilogram),
                Pair("q_water", QuantityFixture.oneLitre)
            ),
            locals = mapOf(
                Pair("x", QuantityFixture.oneKilogram)
            ),
            body = EProcess(
                name = "carrot_production",
                products = listOf(
                    ETechnoExchange(
                        EQuantityAdd(
                            EDataRef("q_carrot"),
                            EDataRef("x"),
                        ), ProductFixture.carrot
                    ),
                ),
                inputs = listOf(
                    ETechnoBlockEntry(ETechnoExchange(EDataRef("q_water"), ProductFixture.water)),
                ),
            )
        )
        val arguments: Map<String, DataExpression<BasicNumber>> = mapOf(
            Pair("q_carrot", QuantityFixture.twoKilograms),
        )
        val expression = EProcessTemplateApplication(template, arguments)
        val reducer = TemplateExpressionReducer(ops, sourceOps)

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected =
            EProcess(
                name = "carrot_production",
                products = listOf(
                    ETechnoExchange(
                        EQuantityScale(ops.pure(3.0), UnitFixture.kg),
                        ProductFixture.carrot.copy(
                            fromProcess =
                            FromProcess(
                                "carrot_production",
                                MatchLabels(emptyMap()),
                                mapOf(
                                    "q_carrot" to QuantityFixture.twoKilograms,
                                    "q_water" to QuantityFixture.oneLitre,
                                )
                            )
                        )
                    ),
                ),
                inputs = listOf(
                    ETechnoBlockEntry(
                        ETechnoExchange(
                            QuantityFixture.oneLitre,
                            ProductFixture.water
                        )
                    ),
                ),
            )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenArgumentDoesNotMatchAnyParam_shouldThrow() {
        // given
        val template = EProcessTemplate(
            params = mapOf(
                Pair("q_carrot", QuantityFixture.oneKilogram),
                Pair("q_water", QuantityFixture.oneLitre)
            ),
            locals = mapOf(
                Pair("x", QuantityFixture.oneKilogram)
            ),
            body = EProcess(
                name = "carrot_production",
                products = listOf(
                    ETechnoExchange(
                        EQuantityAdd(
                            EDataRef("q_carrot"),
                            EDataRef("x"),
                        ), ProductFixture.carrot
                    ),
                ),
                inputs = listOf(
                    ETechnoBlockEntry(ETechnoExchange(EDataRef("q_water"), ProductFixture.water)),
                ),
            )
        )
        val arguments: Map<String, DataExpression<BasicNumber>> = mapOf(
            Pair("foo", QuantityFixture.twoKilograms),
        )
        val expression = EProcessTemplateApplication(template, arguments)
        val reducer = TemplateExpressionReducer(ops, sourceOps)

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { reducer.reduce(expression) }
        assertEquals("unknown parameters: [foo]", e.message)
    }
}
