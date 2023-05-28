package ch.kleis.lcaplugin.core.lang.expression.optics

import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.ProductFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceFixture
import org.junit.Assert.assertEquals
import org.junit.Test

class QuantityExpressionTest {
    @Test
    fun optics_quantityReferencesInQuantity_getAll() {
        // given
        val expression = EQuantityAdd(
            EQuantityRef("x"),
            EQuantityDiv(
                EQuantityRef("y"),
                EQuantityRef("z"),
            )
        )

        // when
        val actual = everyQuantityRefInQuantityExpression.getAll(expression)

        // then
        val expected = listOf(EQuantityRef("x"), EQuantityRef("y"), EQuantityRef("z"))
        assertEquals(expected, actual)
    }

    @Test
    fun optics_quantityReferencesInQuantity_shouldHandleComplexExpressions() {
        // given
        val expression = EQuantityAdd(
            EQuantityRef("x"),
            EQuantityDiv(
                EQuantityRef("y"),
                EQuantityRef("x"),
            )
        )
        val map: (EQuantityRef) -> QuantityExpression = { ref ->
            if (ref.name == "x") QuantityFixture.oneKilogram else ref
        }

        // when
        val actual = everyQuantityRefInQuantityExpression.modify(expression, map)

        // then
        val expected = EQuantityAdd(
            QuantityFixture.oneKilogram,
            EQuantityDiv(
                EQuantityRef("y"),
                QuantityFixture.oneKilogram,
            )
        )
        assertEquals(expected, actual)
    }
}
