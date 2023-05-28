package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.ProductFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import org.junit.Assert.assertEquals
import org.junit.Test

class HelperTest {
    @Test
    fun substitute_whenProcessWithStringRef_shouldSubstitute() {
        // given
        val ref = EStringRef("class")
        val body = EProcess(
            name = "p",
            labels = emptyMap(),
            products = listOf(
                ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot)
            ),
            inputs = listOf(
                ETechnoExchange(
                    QuantityFixture.oneKilogram,
                    ProductFixture.carrot.copy(
                        matchLabels = MatchLabels(mapOf("class" to ref))
                    )
                )
            ),
            biosphere = emptyList(),
        )
        val helper = Helper()

        // when
        val actual = helper.substitute("class", EStringLiteral("foo"), body)

        // then
        val expected = EProcess(
            name = "p",
            labels = emptyMap(),
            products = listOf(
                ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot)
            ),
            inputs = listOf(
                ETechnoExchange(
                    QuantityFixture.oneKilogram,
                    ProductFixture.carrot.copy(
                        matchLabels = MatchLabels(mapOf("class" to EStringLiteral("foo")))
                    )
                )
            ),
            biosphere = emptyList(),
        )
        assertEquals(expected, actual)
    }


    @Test
    fun substitute_whenProcess_shouldSubstitute() {
        // given
        val ref = EQuantityRef("q")
        val body = EProcess(
            name = "p",
            labels = emptyMap(),
            products = listOf(
                ETechnoExchange(ref, ProductFixture.carrot)
            ),
            inputs = listOf(
                ETechnoExchange(ref, ProductFixture.carrot)
            ),
            biosphere = listOf(
                EBioExchange(ref, SubstanceFixture.propanol)
            ),
        )
        val helper = Helper()

        // when
        val actual = helper.substitute("q", QuantityFixture.oneKilogram, body)

        // then
        val expected = EProcess(
            name = "p",
            labels = emptyMap(),
            products = listOf(
                ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot)
            ),
            inputs = listOf(
                ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot)
            ),
            biosphere = listOf(
                EBioExchange(QuantityFixture.oneKilogram, SubstanceFixture.propanol)
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun substitute_whenProcessWithConstrainedProduct_shouldSubstitute() {
        // given
        val ref = EQuantityRef("q")
        val body = EProcess(
            name = "p",
            labels = emptyMap(),
            products = listOf(
                ETechnoExchange(
                    ref,
                    EProductSpec(
                        "carrot",
                        UnitFixture.kg,
                        FromProcess(
                            "carrot_production",
                            mapOf(
                                Pair("x", ref)
                            ),
                        )
                    )
                )
            ),
            inputs = emptyList(),
            biosphere = emptyList(),
        )
        val helper = Helper()

        // when
        val actual = helper.substitute("q", QuantityFixture.oneKilogram, body)

        // then
        val expected = EProcess(
            name = "p",
            labels = emptyMap(),
            products = listOf(
                ETechnoExchange(
                    QuantityFixture.oneKilogram,
                    EProductSpec(
                        "carrot",
                        UnitFixture.kg,
                        FromProcess(
                            "carrot_production",
                            mapOf(
                                Pair("x", QuantityFixture.oneKilogram)
                            ),
                        )
                    )
                )
            ),
            inputs = emptyList(),
            biosphere = emptyList(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun unboundedReferences() {
        // given
        val expression = ESystem(
            listOf(
                EProcess(
                    name = "p",
                    labels = emptyMap(),
                    listOf(ETechnoExchange(EQuantityRef("quantity"), ProductFixture.carrot)),
                    listOf(
                        ETechnoExchange(
                            EQuantityScale(
                                1.0,
                                EQuantityMul(EQuantityRef("ua"), EQuantityRef("ub"))
                            ), EProductSpec(
                                "product",
                            )
                        ),
                        ETechnoExchange(
                            QuantityFixture.oneLitre, EProductSpec(
                                "water",
                                UnitFixture.l,
                                FromProcess("template", emptyMap())
                            )
                        ),
                    ),
                    emptyList(),
                )
            ),
            listOf(
                ESubstanceCharacterization(
                    EBioExchange(QuantityFixture.oneKilogram, ESubstanceSpec("substance")),
                    listOf(
                        EImpact(
                            EQuantityAdd(
                                EQuantityScale(3.0, EQuantityRef("qa")),
                                EQuantityRef("qb")
                            ), EIndicatorSpec("indicator")
                        )
                    ),
                )
            ),
        )
        val helper = Helper()

        // when
        val actual = helper.allRequiredRefs(expression)

        // then
        val expected = setOf(
            "quantity", "ua", "ub", "qa", "qb"
        )
        assertEquals(expected, actual)
    }
}
