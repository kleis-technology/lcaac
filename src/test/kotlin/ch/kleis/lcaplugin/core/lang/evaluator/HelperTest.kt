package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.ProductFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import org.junit.Assert.assertEquals
import org.junit.Test

class HelperTest {
    @Test
    fun unboundedReferences() {
        // given
        val expression = ESystem(
            listOf(
                EProcess(
                    listOf(ETechnoExchange(EQuantityRef("quantity"), ProductFixture.carrot)),
                    listOf(
                        ETechnoExchange(EQuantityLiteral(
                            1.0,
                            EUnitMul(EUnitRef("ua"), EUnitRef("ub"))
                        ), EProductRef("product")),
                        ETechnoExchange(QuantityFixture.oneLitre, EConstrainedProduct(
                            ProductFixture.water,
                            FromProcessRef(ETemplateRef("template"), emptyMap())
                        )),
                    ),
                    emptyList(),
                )
            ),
            listOf(
                ESubstanceCharacterization(
                    EBioExchange(QuantityFixture.oneKilogram, ESubstanceRef("substance")),
                    listOf(
                        EImpact(EQuantityAdd(
                            EQuantityRef("qa"),
                            EQuantityRef("qb")
                        ), EIndicatorRef("indicator"))
                    ),
                )
            ),
        )
        val helper = Helper()

        // when
        val actual = helper.unboundedReferences(expression)

        // then
        val expected = setOf(
            "quantity", "ua", "ub", "product", "template", "substance", "qa", "qb", "indicator"
        )
        assertEquals(expected, actual)
    }
}
