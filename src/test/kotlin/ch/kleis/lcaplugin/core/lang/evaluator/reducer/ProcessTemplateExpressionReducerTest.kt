package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.*
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class ProcessTemplateExpressionReducerTest {

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
                labels = emptyMap(),
                products = listOf(
                    ETechnoExchange(
                        EQuantityAdd(
                            EDataRef("q_carrot"),
                            EDataRef("x"),
                        ), ProductFixture.carrot
                    ),
                ),
                inputs = listOf(
                    ETechnoExchange(EDataRef("q_water"), ProductFixture.water),
                ),
                biosphere = emptyList(),
                impacts = emptyList(),
            )
        )
        val arguments: Map<String, DataExpression> = mapOf(
            Pair("q_carrot", QuantityFixture.twoKilograms),
        )
        val expression = EProcessTemplateApplication(template, arguments)
        val reducer = TemplateExpressionReducer()

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProcessFinal(
            EProcess(
                name = "carrot_production",
                labels = emptyMap(),
                products = listOf(
                    ETechnoExchange(
                        EQuantityScale(3.0, UnitFixture.kg),
                        ProductFixture.carrot.copy(
                            fromProcess =
                            FromProcess(
                                "carrot_production",
                                MatchLabels.EMPTY,
                                mapOf(
                                    "q_carrot" to QuantityFixture.twoKilograms,
                                    "q_water" to QuantityFixture.oneLitre,
                                )
                            )
                        )
                    ),
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneLitre,
                        ProductFixture.water
                    ),
                ),
                biosphere = emptyList(),
                impacts = emptyList(),
            )
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
                labels = emptyMap(),
                products = listOf(
                    ETechnoExchange(
                        EQuantityAdd(
                            EDataRef("q_carrot"),
                            EDataRef("x"),
                        ), ProductFixture.carrot
                    ),
                ),
                inputs = listOf(
                    ETechnoExchange(EDataRef("q_water"), ProductFixture.water),
                ),
                biosphere = emptyList(),
                impacts = emptyList(),
            )
        )
        val arguments: Map<String, DataExpression> = mapOf(
            Pair("foo", QuantityFixture.twoKilograms),
        )
        val expression = EProcessTemplateApplication(template, arguments)
        val reducer = TemplateExpressionReducer()

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { reducer.reduce(expression) }
        assertEquals("unknown parameters: [foo]", e.message)
    }

    @Test
    fun reduce_whenProcessFinal_shouldRemainUnchanged() {
        // given
        val expression = EProcessFinal(ProcessFixture.carrotProduction)
        val reducer = TemplateExpressionReducer()

        // when
        val actual = reducer.reduce(expression)

        // then
        assertEquals(expression, actual)
    }

    @Test
    fun reduce_whenTemplate_shouldRemainUnchanged() {
        // given
        val expression = TemplateFixture.carrotProduction
        val reducer = TemplateExpressionReducer()

        // when
        val actual = reducer.reduce(expression)

        // then
        assertEquals(expression, actual)
    }
}
