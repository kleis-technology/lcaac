package ch.kleis.lcaplugin.core.lang.dimension

import org.junit.Test
import kotlin.test.assertEquals


class UnitSymbolTest {
    @Test
    fun multiply() {
        // given
        val a = UnitSymbol(mapOf("a" to 1.0), 2.0)
        val b = UnitSymbol(mapOf("b" to 1.0), 3.0)

        // when
        val actual = a.multiply(b)

        // then
        val expected = UnitSymbol(mapOf("a" to 1.0, "b" to 1.0), 6.0)
        assertEquals(expected, actual)
    }

    @Test
    fun divide() {
        // given
        val a = UnitSymbol(mapOf("a" to 1.0), 4.0)
        val b = UnitSymbol(mapOf("b" to 1.0), 2.0)

        // when
        val actual = a.divide(b)

        // then
        val expected = UnitSymbol(mapOf("a" to 1.0, "b" to -1.0), 2.0)
        assertEquals(expected, actual)
    }

    @Test
    fun pow() {
        // given
        val a = UnitSymbol(mapOf("a" to 1.0), 2.0)

        // when
        val actual = a.pow(2.0)

        // then
        val expected = UnitSymbol(mapOf("a" to 2.0), 4.0)
        assertEquals(expected, actual)
    }

    @Test
    fun scale() {
        // given
        val a = UnitSymbol(mapOf("a" to 1.0), 2.0)

        // when
        val actual = a.scale(2.0)

        // then
        val expected = UnitSymbol(mapOf("a" to 1.0), 4.0)
        assertEquals(expected, actual)
    }
}
