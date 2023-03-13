package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.ProductFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceFixture
import org.junit.Assert.assertEquals
import org.junit.Test

class HelperTest {
    @Test
    fun substitute_whenProcess_shouldSubstitute() {
        // given
        val ref = EQuantityRef("q")
        val body = EProcess(
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
            products = listOf(
                ETechnoExchange(
                    ref,
                    EConstrainedProduct(
                        ProductFixture.carrot,
                        FromProcessRef(
                            ETemplateRef("carrot_production"),
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
            products = listOf(
                ETechnoExchange(
                    QuantityFixture.oneKilogram,
                    EConstrainedProduct(
                        ProductFixture.carrot,
                        FromProcessRef(
                            ETemplateRef("carrot_production"),
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
                    listOf(ETechnoExchange(EQuantityRef("quantity"), ProductFixture.carrot)),
                    listOf(
                        ETechnoExchange(
                            EQuantityLiteral(
                                1.0,
                                EUnitMul(EUnitRef("ua"), EUnitRef("ub"))
                            ), EProductRef("product")
                        ),
                        ETechnoExchange(
                            QuantityFixture.oneLitre, EConstrainedProduct(
                                ProductFixture.water,
                                FromProcessRef(ETemplateRef("template"), emptyMap())
                            )
                        ),
                    ),
                    emptyList(),
                )
            ),
            listOf(
                ESubstanceCharacterization(
                    EBioExchange(QuantityFixture.oneKilogram, ESubstanceRef("substance")),
                    listOf(
                        EImpact(
                            EQuantityAdd(
                                EQuantityRef("qa"),
                                EQuantityRef("qb")
                            ), EIndicatorRef("indicator")
                        )
                    ),
                )
            ),
        )
        val helper = Helper()

        // when
        val actual = helper.allUnboundedReferencesButProductRefs(expression)

        // then
        val expected = setOf(
            "quantity", "ua", "ub", "template", "substance", "qa", "qb", "indicator"
        )
        assertEquals(expected, actual)
    }
}
