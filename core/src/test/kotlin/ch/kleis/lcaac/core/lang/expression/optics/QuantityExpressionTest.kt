package ch.kleis.lcaac.core.lang.expression.optics

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EQuantityAdd
import ch.kleis.lcaac.core.lang.expression.EQuantityDiv
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class QuantityExpressionTest {
    @Test
    fun optics_quantityReferencesInQuantity_getAll() {
        // given
        val expression = EQuantityAdd<BasicNumber>(
            EDataRef("x"),
            EQuantityDiv(
                EDataRef("y"),
                EDataRef("z"),
            )
        )

        // when
        val actual = everyDataRefInDataExpression<BasicNumber>().getAll(expression)

        // then
        val expected = listOf(EDataRef<BasicNumber>("x"), EDataRef("y"), EDataRef("z"))
        assertEquals(expected, actual)
    }

    @Test
    fun optics_quantityReferencesInQuantity_shouldHandleComplexExpressions() {
        // given
        val expression = EQuantityAdd<BasicNumber>(
            EDataRef("x"),
            EQuantityDiv(
                EDataRef("y"),
                EDataRef("x"),
            )
        )
        val map: (EDataRef<BasicNumber>) -> DataExpression<BasicNumber> = { ref ->
            if (ref.name == "x") QuantityFixture.oneKilogram else ref
        }

        // when
        val actual = everyDataRefInDataExpression<BasicNumber>().modify(expression, map)

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
