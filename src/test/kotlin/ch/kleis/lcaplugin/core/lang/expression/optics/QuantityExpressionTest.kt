package ch.kleis.lcaplugin.core.lang.expression.optics

import ch.kleis.lcaplugin.core.lang.expression.DataExpression
import ch.kleis.lcaplugin.core.lang.expression.EDataRef
import ch.kleis.lcaplugin.core.lang.expression.EQuantityAdd
import ch.kleis.lcaplugin.core.lang.expression.EQuantityDiv
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import org.junit.Assert.assertEquals
import org.junit.Test

class QuantityExpressionTest {
    @Test
    fun optics_quantityReferencesInQuantity_getAll() {
        // given
        val expression = EQuantityAdd(
            EDataRef("x"),
            EQuantityDiv(
                EDataRef("y"),
                EDataRef("z"),
            )
        )

        // when
        val actual = everyDataRefInDataExpression.getAll(expression)

        // then
        val expected = listOf(EDataRef("x"), EDataRef("y"), EDataRef("z"))
        assertEquals(expected, actual)
    }

    @Test
    fun optics_quantityReferencesInQuantity_shouldHandleComplexExpressions() {
        // given
        val expression = EQuantityAdd(
            EDataRef("x"),
            EQuantityDiv(
                EDataRef("y"),
                EDataRef("x"),
            )
        )
        val map: (EDataRef) -> DataExpression = { ref ->
            if (ref.name == "x") QuantityFixture.oneKilogram else ref
        }

        // when
        val actual = everyDataRefInDataExpression.modify(expression, map)

        // then
        val expected = EQuantityAdd(
            QuantityFixture.oneKilogram,
            EQuantityDiv(
                EDataRef("y"),
                QuantityFixture.oneKilogram,
            )
        )
        assertEquals(expected, actual)
    }
}
