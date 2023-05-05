package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.expression.EQuantityLiteral
import ch.kleis.lcaplugin.core.lang.expression.ETechnoExchange
import ch.kleis.lcaplugin.core.lang.fixture.*
import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.lang.value.TechnoExchangeValue
import org.junit.Assert.assertEquals
import org.junit.Test

class ToValueTest {

    @Test
    fun toValue_whenETechnoExchange() {
        // given
        val allocation = EQuantityLiteral(10.0, UnitFixture.percent)
        val expression = ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot, allocation)
        // when
        val actual = expression.toValue()
        // then
        val allocationValue = QuantityValue(10.0, UnitValueFixture.percent)
        val expected =
            TechnoExchangeValue(QuantityValueFixture.oneKilogram, ProductValueFixture.carrot, allocationValue)

        assertEquals(expected, actual)
    }
}
