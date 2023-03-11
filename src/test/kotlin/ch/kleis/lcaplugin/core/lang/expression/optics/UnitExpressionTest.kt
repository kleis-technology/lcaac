package ch.kleis.lcaplugin.core.lang.expression.optics

import ch.kleis.lcaplugin.core.lang.expression.EUnitDiv
import ch.kleis.lcaplugin.core.lang.expression.EUnitMul
import ch.kleis.lcaplugin.core.lang.expression.EUnitPow
import ch.kleis.lcaplugin.core.lang.expression.EUnitRef
import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitExpressionTest {
    @Test
    fun optics_unitReferences_shouldGetAll() {
        // given
        val expression = EUnitMul(
            EUnitRef("a"),
            EUnitPow(
                EUnitDiv(EUnitRef("b"), EUnitRef("c")),
                2.0
            )
        )

        // when
        val actual = everyUnitRefInUnitExpression.getAll(expression)

        // then
        val expected = listOf(EUnitRef("a"), EUnitRef("b"), EUnitRef("c"))
        assertEquals(expected, actual)
    }

    @Test
    fun optics_unitReferences_whenModify_shouldHandleComplexExpression() {
        // given
        val expression = EUnitMul(
            EUnitRef("a"),
            EUnitPow(
                EUnitDiv(EUnitRef("b"), EUnitRef("a")),
                2.0
            )
        )

        // when
        val actual = everyUnitRefInUnitExpression.modify(expression) {
            if (it.name == "a") UnitFixture.kg else it
        }

        // then
        val expected = EUnitMul(
            UnitFixture.kg,
            EUnitPow(
                EUnitDiv(EUnitRef("b"), UnitFixture.kg),
                2.0
            )
        )
        assertEquals(expected, actual)
    }
}
