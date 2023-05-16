package ch.kleis.lcaplugin.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.ProcessFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import junit.framework.TestCase.assertEquals
import org.junit.Test
import kotlin.test.assertNull


class CompleteDefaultArgumentsTest {
    @Test
    fun apply_whenEProcessTemplateApplication() {
        // given
        val params = mapOf(
            "q_water" to QuantityFixture.oneLitre,
            "q_pesticide" to QuantityFixture.oneKilogram,
        )
        val symbolTable = SymbolTable(
            processTemplates = Register.from(
                mapOf(
                    "carrot_production" to EProcessTemplate(
                        params = params,
                        locals = emptyMap(),
                        body = ProcessFixture.carrotProduction,
                    )
                )
            )
        )
        val completeDefaultArguments = CompleteDefaultArguments(symbolTable)
        val expression = EProcessTemplateApplication(
            EProcessTemplate(
                params = emptyMap(),
                locals = emptyMap(),
                body = EProcess(
                    "salad_production",
                    products = emptyList(),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                UnitFixture.kg,
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
            ), emptyMap()
        )
        val everyInputProduct = ProcessTemplateExpression
            .eProcessTemplateApplication
            .template
            .eProcessTemplate
            .body
            .inputs
            .compose(Every.list())
            .compose(ETechnoExchange.product)

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.fromProcessRef

        // then
        val expected = FromProcessRef(
            "carrot_production",
            params.plus("q_water" to QuantityFixture.twoLitres)
        )
        assertEquals(expected, actual)
    }

    @Test
    fun apply_whenMissingArgument_shouldCompleteArgumentsWithDefaultValues() {
        // given
        val params = mapOf(
            "q_water" to QuantityFixture.oneLitre,
            "q_pesticide" to QuantityFixture.oneKilogram,
        )
        val symbolTable = SymbolTable(
            processTemplates = Register.from(
                mapOf(
                    "carrot_production" to EProcessTemplate(
                        params = params,
                        locals = emptyMap(),
                        body = ProcessFixture.carrotProduction,
                    )
                )
            )
        )
        val completeDefaultArguments = CompleteDefaultArguments(symbolTable)
        val expression = EProcessTemplate(
            params = emptyMap(),
            locals = emptyMap(),
            body = EProcess(
                "salad_production",
                products = emptyList(),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EProductSpec(
                            "carrot",
                            UnitFixture.kg,
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
        val everyInputProduct = ProcessTemplateExpression
            .eProcessTemplateApplication
            .template
            .eProcessTemplate
            .body
            .inputs
            .compose(Every.list())
            .compose(ETechnoExchange.product)

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.fromProcessRef

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
        val symbolTable = SymbolTable(
            processTemplates = Register.from(
                mapOf(
                    "carrot_production" to EProcessTemplate(
                        params = params,
                        locals = emptyMap(),
                        body = ProcessFixture.carrotProduction,
                    )
                )
            )
        )
        val completeDefaultArguments = CompleteDefaultArguments(symbolTable)
        val expression = EProcessTemplate(
            params = emptyMap(),
            locals = emptyMap(),
            body = EProcess(
                "salad_production",
                products = emptyList(),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EProductSpec(
                            "carrot",
                            UnitFixture.kg,
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
        val everyInputProduct = ProcessTemplateExpression
            .eProcessTemplateApplication
            .template
            .eProcessTemplate
            .body
            .inputs
            .compose(Every.list())
            .compose(ETechnoExchange.product)

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.fromProcessRef

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
        val symbolTable = SymbolTable(
            processTemplates = Register.from(
                mapOf(
                    "carrot_production" to EProcessTemplate(
                        params = params,
                        locals = emptyMap(),
                        body = ProcessFixture.carrotProduction,
                    )
                )
            )
        )
        val completeDefaultArguments = CompleteDefaultArguments(symbolTable)
        val expression = EProcessTemplate(
            params = emptyMap(),
            locals = emptyMap(),
            body = EProcess(
                "salad_production",
                products = emptyList(),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EProductSpec(
                            "carrot",
                            UnitFixture.kg,
                        )
                    )
                ),
                biosphere = emptyList(),
            )
        )
        val everyInputProduct = ProcessTemplateExpression
            .eProcessTemplateApplication
            .template
            .eProcessTemplate
            .body
            .inputs
            .compose(Every.list())
            .compose(ETechnoExchange.product)

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.fromProcessRef

        // then
        assertNull(actual)
    }
}
