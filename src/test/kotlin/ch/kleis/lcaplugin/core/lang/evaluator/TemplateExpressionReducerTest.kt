package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.fixture.*
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class TemplateExpressionReducerTest {

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
                products = listOf(
                    ETechnoExchange(
                        EQuantityAdd(
                            EQuantityRef("q_carrot"),
                            EQuantityRef("x"),
                        ), ProductFixture.carrot
                    ),
                ),
                inputs = listOf(
                    ETechnoExchange(EQuantityRef("q_water"), ProductFixture.water),
                ),
                biosphere = emptyList(),
            )
        )
        val arguments = mapOf(
            Pair("q_carrot", QuantityFixture.twoKilograms),
        )
        val expression = EInstance(template, arguments)
        val reducer = TemplateExpressionReducer()

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProcessFinal(
            EProcess(
                products = listOf(
                    ETechnoExchange(
                        EQuantityLiteral(3.0, UnitFixture.kg),
                        ProductFixture.carrot
                    ),
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneLitre,
                        ProductFixture.water
                    ),
                ),
                biosphere = emptyList(),
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
                products = listOf(
                    ETechnoExchange(
                        EQuantityAdd(
                            EQuantityRef("q_carrot"),
                            EQuantityRef("x"),
                        ), ProductFixture.carrot
                    ),
                ),
                inputs = listOf(
                    ETechnoExchange(EQuantityRef("q_water"), ProductFixture.water),
                ),
                biosphere = emptyList(),
            )
        )
        val arguments = mapOf(
            Pair("foo", QuantityFixture.twoKilograms),
        )
        val expression = EInstance(template, arguments)
        val reducer = TemplateExpressionReducer()

        // when/then
        try {
            reducer.reduce(expression)
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals("unknown parameters: [foo]", e.message)
        }
    }

    @Test
    fun reduce_whenTemplateRef_shouldReadEnv() {
        // given
        val template = TemplateFixture.carrotProduction
        val expression = ETemplateRef("carrot_production")
        val reducer = TemplateExpressionReducer(
            templateEnvironment = Environment(
                hashMapOf(
                    Pair("carrot_production", template)
                )
            )
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        assertEquals(template, actual)
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
