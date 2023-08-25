package ch.kleis.lcaplugin.core.lang.evaluator.step

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import org.junit.Assert.assertEquals
import org.junit.Test


class ReduceLabelSelectorsTest {
    private val ops = BasicOperations.INSTANCE

    @Test
    fun reduce_whenPassingByArguments() {
        // given
        val instance = EProcessTemplateApplication(
            template = EProcessTemplate(
                params = mapOf("geo" to EStringLiteral("GLO")),
                body = EProcess(
                    name = "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            quantity = QuantityFixture.oneKilogram,
                            product = EProductSpec(
                                name = "carrot",
                                referenceUnit = QuantityFixture.oneKilogram,
                                fromProcess = FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                )
                            )
                        )
                    ),
                )
            ),
            mapOf("geo" to EStringLiteral("FR")),
        )
        val reduceLabelSelectors = ReduceLabelSelectors(SymbolTable(), ops)

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            EProcessTemplate(
                params = mapOf("geo" to EStringLiteral("GLO")),
                body = EProcess(
                    "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EStringLiteral("FR"))),
                                )
                            )
                        )
                    ),
                )
            ),
            mapOf("geo" to EStringLiteral("FR")),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPassingByDefaultParams() {
        // given
        val instance = EProcessTemplateApplication(
            template = EProcessTemplate(
                params = mapOf("geo" to EStringLiteral("GLO")),
                body = EProcess(
                    name = "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        val reduceLabelSelectors = ReduceLabelSelectors(SymbolTable(), ops)

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            template = EProcessTemplate(
                params = mapOf("geo" to EStringLiteral("GLO")),
                body = EProcess(
                    name = "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EStringLiteral("GLO"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPassingByLocals() {
        // given
        val instance = EProcessTemplateApplication(
            template = EProcessTemplate(
                locals = mapOf("geo" to EStringLiteral("GLO")),
                body = EProcess(
                    "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        val reduceLabelSelectors = ReduceLabelSelectors(SymbolTable(), ops)

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            template = EProcessTemplate(
                locals = mapOf("geo" to EStringLiteral("GLO")),
                body = EProcess(
                    name = "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EStringLiteral("GLO"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPassingByGlobalVariables() {
        // given
        val instance = EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    name = "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )

        val reduceLabelSelectors = ReduceLabelSelectors(
            SymbolTable(
                data = Register.from(mapOf("geo" to EStringLiteral("FR")))
            ),
            ops,
        )

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EStringLiteral("FR"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPassingByProcessLabels() {
        // given
        val instance = EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    "salad_production",
                    labels = mapOf("geo" to EStringLiteral("FR")),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        val reduceLabelSelectors = ReduceLabelSelectors(SymbolTable(), ops)

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    "salad_production",
                    labels = mapOf("geo" to EStringLiteral("FR")),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EStringLiteral("FR"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        assertEquals(expected, actual)
    }
}
