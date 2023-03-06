package ch.kleis.lcaplugin.core.lang

import org.junit.Assert.assertEquals
import org.junit.Test

class VProductTest {
    @Test
    fun testEquals_shouldReturnTrueWhenNameAndDimensionMatch() {
        // given
        val kg = VUnit("kg", 1.0, Dimension.of("mass"))
        val g = VUnit("g", 1E-3, Dimension.of("mass"))

        val a = VProduct("a", kg)
        val aWithDifferentUnit = VProduct("a", g)
        // when + then
        assertEquals(a,aWithDifferentUnit)
    }
}