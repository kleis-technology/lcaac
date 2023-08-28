package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.ProductFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import org.junit.Assert.assertEquals
import org.junit.Test

class HelperTest {
    private val ops = BasicOperations

    @Test
    fun substitute_whenProcessWithStringRef_shouldSubstitute() {
        // given
        val ref = EDataRef<BasicNumber>("class")
        val body = EProcess(
            name = "p",
            products = listOf(
                ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot)
            ),
            inputs = listOf(
                ETechnoExchange(
                    QuantityFixture.oneKilogram,
                    ProductFixture.carrot.copy(
                        fromProcess = FromProcess(
                            name = "another_process",
                            matchLabels = MatchLabels(mapOf("class" to ref)),
                        ),
                    )
                )
            ),
        )
        val helper = Helper<BasicNumber>()

        // when
        val actual = helper.substitute("class", EStringLiteral("foo"), body)

        // then
        val expected = EProcess(
            name = "p",
            products = listOf(
                ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot)
            ),
            inputs = listOf(
                ETechnoExchange(
                    QuantityFixture.oneKilogram,
                    ProductFixture.carrot.copy(
                        fromProcess = FromProcess(
                            name = "another_process",
                            matchLabels = MatchLabels(mapOf("class" to EStringLiteral("foo"))),
                        ),
                    )
                )
            ),
        )
        assertEquals(expected, actual)
    }


    @Test
    fun substitute_whenProcess_shouldSubstitute() {
        // given
        val ref = EDataRef<BasicNumber>("q")
        val body = EProcess(
            name = "p",
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
        val helper = Helper<BasicNumber>()

        // when
        val actual = helper.substitute("q", QuantityFixture.oneKilogram, body)

        // then
        val expected = EProcess(
            name = "p",
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
        val ref = EDataRef<BasicNumber>("q")
        val body = EProcess(
            name = "p",
            products = listOf(
                ETechnoExchange(
                    ref,
                    EProductSpec(
                        "carrot",
                        UnitFixture.kg,
                        FromProcess(
                            "carrot_production",
                            MatchLabels(emptyMap()),
                            mapOf(
                                Pair("x", ref)
                            ),
                        )
                    )
                )
            ),
        )
        val helper = Helper<BasicNumber>()

        // when
        val actual = helper.substitute("q", QuantityFixture.oneKilogram, body)

        // then
        val expected = EProcess(
            name = "p",
            products = listOf(
                ETechnoExchange(
                    QuantityFixture.oneKilogram,
                    EProductSpec(
                        "carrot",
                        UnitFixture.kg,
                        FromProcess(
                            "carrot_production",
                            MatchLabels(emptyMap()),
                            mapOf(
                                Pair("x", QuantityFixture.oneKilogram)
                            ),
                        )
                    )
                )
            ),
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
                    products = listOf(ETechnoExchange(EDataRef("quantity"), ProductFixture.carrot)),
                    inputs = listOf(
                        ETechnoExchange(
                            EQuantityScale(
                                ops.pure(1.0),
                                EQuantityMul(EDataRef("ua"), EDataRef("ub"))
                            ), EProductSpec(
                            "product",
                        )
                        ),
                        ETechnoExchange(
                            QuantityFixture.oneLitre, EProductSpec(
                            "water",
                            UnitFixture.l,
                            FromProcess(name = "template", matchLabels = MatchLabels(emptyMap())),
                        )
                        ),
                    ),
                )
            ),
            listOf(
                ESubstanceCharacterization(
                    EBioExchange(QuantityFixture.oneKilogram, ESubstanceSpec("substance")),
                    listOf(
                        EImpact(
                            EQuantityAdd(
                                EQuantityScale(ops.pure(3.0), EDataRef("qa")),
                                EDataRef("qb")
                            ), EIndicatorSpec("indicator")
                        )
                    ),
                )
            ),
        )
        val helper = Helper<BasicNumber>()

        // when
        val actual = helper.allRequiredRefs(expression)

        // then
        val expected = setOf(
            "quantity", "ua", "ub", "qa", "qb"
        )
        assertEquals(expected, actual)
    }
}
