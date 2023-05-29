package ch.kleis.lcaplugin.core.lang.evaluator.step

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import org.junit.Assert.assertEquals
import org.junit.Test


class ReduceLabelSelectorsTest {
    @Test
    fun reduce_whenPassingByArguments() {
        // given
        val instance = EProcessTemplateApplication(
            EProcessTemplate(
                params = mapOf("geo" to EStringLiteral("GLO")),
                locals = emptyMap(),
                EProcess(
                    "salad_production",
                    labels = emptyMap(),
                    products = emptyList(),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    "carrot_production",
                                    MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                    emptyMap(),
                                )
                            )
                        )
                    ),
                    biosphere = emptyList(),
                )
            ),
            mapOf("geo" to EStringLiteral("FR")),
        )
        val reduceLabelSelectors = ReduceLabelSelectors(SymbolTable())

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            EProcessTemplate(
                params = mapOf("geo" to EStringLiteral("GLO")),
                locals = emptyMap(),
                EProcess(
                    "salad_production",
                    labels = emptyMap(),
                    products = emptyList(),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    "carrot_production",
                                    MatchLabels(mapOf("geo" to EStringLiteral("FR"))),
                                    emptyMap(),
                                )
                            )
                        )
                    ),
                    biosphere = emptyList(),
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
            EProcessTemplate(
                params = mapOf("geo" to EStringLiteral("GLO")),
                locals = emptyMap(),
                EProcess(
                    "salad_production",
                    labels = emptyMap(),
                    products = emptyList(),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    "carrot_production",
                                    MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                    emptyMap(),
                                )
                            )
                        )
                    ),
                    biosphere = emptyList(),
                )
            ),
            emptyMap(),
        )
        val reduceLabelSelectors = ReduceLabelSelectors(SymbolTable())

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            EProcessTemplate(
                params = mapOf("geo" to EStringLiteral("GLO")),
                locals = emptyMap(),
                EProcess(
                    "salad_production",
                    labels = emptyMap(),
                    products = emptyList(),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    "carrot_production",
                                    MatchLabels(mapOf("geo" to EStringLiteral("GLO"))),
                                    emptyMap(),
                                )
                            )
                        )
                    ),
                    biosphere = emptyList(),
                )
            ),
            emptyMap(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPassingByLocals() {
        // given
        val instance = EProcessTemplateApplication(
            EProcessTemplate(
                params = emptyMap(),
                locals = mapOf("geo" to EStringLiteral("GLO")),
                EProcess(
                    "salad_production",
                    labels = emptyMap(),
                    products = emptyList(),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    "carrot_production",
                                    MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                    emptyMap(),
                                )
                            )
                        )
                    ),
                    biosphere = emptyList(),
                )
            ),
            emptyMap(),
        )
        val reduceLabelSelectors = ReduceLabelSelectors(SymbolTable())

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            EProcessTemplate(
                params = emptyMap(),
                locals = mapOf("geo" to EStringLiteral("GLO")),
                EProcess(
                    "salad_production",
                    labels = emptyMap(),
                    products = emptyList(),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    "carrot_production",
                                    MatchLabels(mapOf("geo" to EStringLiteral("GLO"))),
                                    emptyMap(),
                                )
                            )
                        )
                    ),
                    biosphere = emptyList(),
                )
            ),
            emptyMap(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPassingByGlobalVariables() {
        // given
        val instance = EProcessTemplateApplication(
            EProcessTemplate(
                params = emptyMap(),
                locals = emptyMap(),
                EProcess(
                    "salad_production",
                    labels = emptyMap(),
                    products = emptyList(),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    "carrot_production",
                                    MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                    emptyMap(),
                                )
                            )
                        )
                    ),
                    biosphere = emptyList(),
                )
            ),
            emptyMap(),
        )
        val reduceLabelSelectors = ReduceLabelSelectors(
            SymbolTable(
                data = Register.from(mapOf("geo" to EStringLiteral("FR")))
            )
        )

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            EProcessTemplate(
                params = emptyMap(),
                locals = emptyMap(),
                EProcess(
                    "salad_production",
                    labels = emptyMap(),
                    products = emptyList(),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    "carrot_production",
                                    MatchLabels(mapOf("geo" to EStringLiteral("FR"))),
                                    emptyMap(),
                                )
                            )
                        )
                    ),
                    biosphere = emptyList(),
                )
            ),
            emptyMap(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPassingByProcessLabels() {
        // given
        val instance = EProcessTemplateApplication(
            EProcessTemplate(
                params = emptyMap(),
                locals = emptyMap(),
                EProcess(
                    "salad_production",
                    labels = mapOf("geo" to EStringLiteral("FR")),
                    products = emptyList(),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    "carrot_production",
                                    MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                    emptyMap(),
                                )
                            )
                        )
                    ),
                    biosphere = emptyList(),
                )
            ),
            emptyMap(),
        )
        val reduceLabelSelectors = ReduceLabelSelectors(SymbolTable())

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            EProcessTemplate(
                params = emptyMap(),
                locals = emptyMap(),
                EProcess(
                    "salad_production",
                    labels = mapOf("geo" to EStringLiteral("FR")),
                    products = emptyList(),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    "carrot_production",
                                    MatchLabels(mapOf("geo" to EStringLiteral("FR"))),
                                    emptyMap(),
                                )
                            )
                        )
                    ),
                    biosphere = emptyList(),
                )
            ),
            emptyMap(),
        )
        assertEquals(expected, actual)
    }
}
