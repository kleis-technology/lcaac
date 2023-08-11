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
                        body = ProcessFixture.carrotProduction,
                    )
                )
            )
        )
        val completeDefaultArguments = CompleteDefaultArguments(symbolTable)
        val expression = EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                UnitFixture.kg,
                                FromProcess(
                                    "carrot_production",
                                    MatchLabels.EMPTY,
                                    mapOf(
                                        "q_water" to QuantityFixture.twoLitres,
                                    )
                                )
                            )
                        )
                    ),
                )
            ),
        )
        val everyInputProduct = ProcessTemplateExpression
            .eProcessTemplateApplication
            .template
            .body
            .inputs
            .compose(Every.list())
            .compose(ETechnoExchange.product)

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.fromProcess

        // then
        val expected = FromProcess(
            "carrot_production",
            MatchLabels.EMPTY,
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
                        body = ProcessFixture.carrotProduction,
                    )
                )
            )
        )
        val completeDefaultArguments = CompleteDefaultArguments(symbolTable)
        val expression = EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                UnitFixture.kg,
                                FromProcess(
                                    "carrot_production",
                                    MatchLabels.EMPTY,
                                    mapOf(
                                        "q_water" to QuantityFixture.twoLitres,
                                    )
                                )
                            )
                        )
                    ),
                )
            ),
        )

        val everyInputProduct = EProcessTemplateApplication
            .template
            .body
            .inputs
            .compose(Every.list())
            .compose(ETechnoExchange.product)

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.fromProcess

        // then
        val expected = FromProcess(
            "carrot_production",
            MatchLabels.EMPTY,
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
                        body = ProcessFixture.carrotProduction,
                    )
                )
            )
        )
        val completeDefaultArguments = CompleteDefaultArguments(symbolTable)
        val expression = EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                UnitFixture.kg,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels.EMPTY,
                                )
                            )
                        )
                    ),
                )
            ),
        )

        val everyInputProduct = EProcessTemplateApplication
            .template
            .body
            .inputs
            .compose(Every.list())
            .compose(ETechnoExchange.product)

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.fromProcess

        // then
        val expected = FromProcess(
            "carrot_production",
            MatchLabels.EMPTY,
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
                        body = ProcessFixture.carrotProduction,
                    )
                )
            )
        )
        val completeDefaultArguments = CompleteDefaultArguments(symbolTable)
        val expression = EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                UnitFixture.kg,
                            )
                        )
                    ),
                )
            ),
        )

        val everyInputProduct = EProcessTemplateApplication
            .template
            .body
            .inputs
            .compose(Every.list())
            .compose(ETechnoExchange.product)

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.fromProcess

        // then
        assertNull(actual)
    }
}
