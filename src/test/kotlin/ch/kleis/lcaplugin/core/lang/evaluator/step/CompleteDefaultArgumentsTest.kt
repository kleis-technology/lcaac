package ch.kleis.lcaplugin.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnconstrainedProductFixture
import ch.kleis.lcaplugin.core.lang.resolver.ProcessResolver
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test


class CompleteDefaultArgumentsTest {
    @Test
    fun apply_whenMissingArgument_shouldCompleteArgumentsWithDefaultValues() {
        // given
        val params = mapOf(
            "q_water" to QuantityFixture.oneLitre,
            "q_pesticide" to QuantityFixture.oneKilogram,
        )
        val processResolver = ProcessResolver(
            SymbolTable(
                processTemplates = Register.from(
                    mapOf(
                        "carrot_production" to EProcessTemplate(
                            params = params,
                            locals = emptyMap(),
                            body = mockk(),
                        )
                    )
                )
            )
        )
        val completeDefaultArguments = CompleteDefaultArguments(processResolver)
        val expression = EProcessTemplate(
            params = emptyMap(),
            locals = emptyMap(),
            body = EProcess(
                "salad_production",
                products = emptyList(),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(
                            UnconstrainedProductFixture.carrot,
                            FromProcessRef(
                                "carrot_production",
                                mapOf(
                                    "q_water" to QuantityFixture.twoLitres,
                                )
                            )
                        )
                    )
                ),
                biosphere = emptyList(),
            )
        )
        val everyInputProduct = ProcessTemplateExpression.eProcessTemplate.body.eProcess.inputs compose
                Every.list() compose
                ETechnoExchange.product.eConstrainedProduct

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.constraint

        // then
        val expected = FromProcessRef(
            "carrot_production",
            params.plus("q_water" to QuantityFixture.twoLitres)
        )
        assertEquals(expected, actual)
    }

    @Test
    fun apply_whenNoArgument_shouldCompleteArgumentsWithDefaultValues() {
        // given
        val params = mapOf(
            "q_water" to QuantityFixture.oneLitre,
            "q_pesticide" to QuantityFixture.oneKilogram,
        )
        val processResolver = ProcessResolver(
            SymbolTable(
                processTemplates = Register.from(
                    mapOf(
                        "carrot_production" to EProcessTemplate(
                            params = params,
                            locals = emptyMap(),
                            body = mockk(),
                        )
                    )
                )
            )
        )
        val completeDefaultArguments = CompleteDefaultArguments(processResolver)
        val expression = EProcessTemplate(
            params = emptyMap(),
            locals = emptyMap(),
            body = EProcess(
                "salad_production",
                products = emptyList(),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(
                            UnconstrainedProductFixture.carrot,
                            FromProcessRef(
                                "carrot_production",
                                emptyMap(),
                            )
                        )
                    )
                ),
                biosphere = emptyList(),
            )
        )
        val everyInputProduct = ProcessTemplateExpression.eProcessTemplate.body.eProcess.inputs compose
                Every.list() compose
                ETechnoExchange.product.eConstrainedProduct

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.constraint

        // then
        val expected = FromProcessRef(
            "carrot_production",
            params,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun apply_whenConstraintIsNone_shouldDoNothing() {
        // given
        val params = mapOf(
            "q_water" to QuantityFixture.oneLitre,
            "q_pesticide" to QuantityFixture.oneKilogram,
        )
        val processResolver = ProcessResolver(
            SymbolTable(
                processTemplates = Register.from(
                    mapOf(
                        "carrot_production" to EProcessTemplate(
                            params = params,
                            locals = emptyMap(),
                            body = mockk(),
                        )
                    )
                )
            )
        )
        val completeDefaultArguments = CompleteDefaultArguments(processResolver)
        val expression = EProcessTemplate(
            params = emptyMap(),
            locals = emptyMap(),
            body = EProcess(
                "salad_production",
                products = emptyList(),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EConstrainedProduct(
                            UnconstrainedProductFixture.carrot,
                            None,
                        )
                    )
                ),
                biosphere = emptyList(),
            )
        )
        val everyInputProduct = ProcessTemplateExpression.eProcessTemplate.body.eProcess.inputs compose
                Every.list() compose
                ETechnoExchange.product.eConstrainedProduct

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.constraint

        // then
        assertEquals(None, actual)
    }
}
