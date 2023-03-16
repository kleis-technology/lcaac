package ch.kleis.lcaplugin.core.lang

import org.junit.Assert.assertEquals
import org.junit.Test

class DimensionTest {
    @Test
    fun multiply() {
        // given
        val a = Dimension.of("a")
        val b = Dimension.of("b")

        // when
        val actual = a.multiply(b)

        // then
        val expected = Dimension(mapOf(
            "a" to 1.0,
            "b" to 1.0,
        ))
        assertEquals(expected, actual)
    }

    @Test
    fun divide() {
        // given
        val a = Dimension.of("a")
        val b = Dimension.of("b")

        // when
        val actual = a.divide(b)

        // then
        val expected = Dimension(mapOf(
            "a" to 1.0,
            "b" to -1.0,
        ))
        assertEquals(expected, actual)
    }

    @Test
    fun pow() {
        // given
        val a = Dimension.of("a")

        // when
        val actual = a.pow(2.0)

        // then
        val expected = Dimension(mapOf(
            "a" to 2.0,
        ))
        assertEquals(expected, actual)
    }
}
