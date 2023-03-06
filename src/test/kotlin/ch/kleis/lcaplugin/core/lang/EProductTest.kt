package ch.kleis.lcaplugin.core.lang

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EProductTest {

    @Test
    fun testEquals() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val g = EUnit("g", 1E-3, Dimension.of("mass"))
        val a = EProduct("a", kg)
        val b = EProduct("a", g)
        // when/then
        assertEquals(a,b)
    }

    @Test
    fun testEquals_notEquals() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val l = EUnit("l", 1E-3, Dimension.of("volume"))
        val a = EProduct("a", kg)
        val b = EProduct("a", l)
        // when/then
        assertNotEquals(a,b)
    }
}