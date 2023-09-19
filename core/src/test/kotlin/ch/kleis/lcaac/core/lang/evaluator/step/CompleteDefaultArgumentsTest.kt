package ch.kleis.lcaac.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaac.core.lang.Register
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.fixture.ProcessFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.fixture.UnitFixture
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
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
                                    MatchLabels(emptyMap()),
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
            .eProcessTemplateApplication<BasicNumber>()
            .template()
            .body()
            .inputs()
            .compose(Every.list())
            .compose(ETechnoExchange.product())

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.fromProcess

        // then
        val expected = FromProcess(
            "carrot_production",
            MatchLabels(emptyMap()),
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
                                    MatchLabels(emptyMap()),
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
            .template<BasicNumber>()
            .body()
            .inputs()
            .compose(Every.list())
            .compose(ETechnoExchange.product())

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.fromProcess

        // then
        val expected = FromProcess(
            "carrot_production",
            MatchLabels(emptyMap()),
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
                                    matchLabels = MatchLabels(emptyMap()),
                                )
                            )
                        )
                    ),
                )
            ),
        )

        val everyInputProduct = EProcessTemplateApplication
            .template<BasicNumber>()
            .body()
            .inputs()
            .compose(Every.list())
            .compose(ETechnoExchange.product())

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.fromProcess

        // then
        val expected = FromProcess(
            "carrot_production",
            MatchLabels(emptyMap()),
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
            .template<BasicNumber>()
            .body()
            .inputs()
            .compose(Every.list())
            .compose(ETechnoExchange.product())

        // when
        val actual = everyInputProduct.firstOrNull(completeDefaultArguments.apply(expression))!!.fromProcess

        // then
        assertNull(actual)
    }
}
