package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.evaluator.Beta
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.ProductFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceFixture
import org.junit.Assert.assertEquals
import org.junit.Test

class BetaTest {
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
        val beta = Beta()

        // when
        val actual = beta.substitute("q", QuantityFixture.oneKilogram, body)

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
        val beta = Beta()

        // when
        val actual = beta.substitute("q", QuantityFixture.oneKilogram, body)

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
}
