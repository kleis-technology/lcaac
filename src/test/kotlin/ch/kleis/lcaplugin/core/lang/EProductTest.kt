package ch.kleis.lcaplugin.core.lang

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EProductTest {
    @Test
    fun equals_whenIdentical_shouldBeEqual() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val a = EProduct("a", kg)
        val b = EProduct("a", kg)
        // when/then
        assertEquals(a,b)
    }

    @Test
    fun equals_whenSameNameAndSameDimension_shouldBeEqual() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val g = EUnit("g", 1E-3, Dimension.of("mass"))
        val a = EProduct("a", kg)
        val b = EProduct("a", g)
        // when/then
        assertEquals(a,b)
    }

    @Test
    fun equals_whenSameNameAndDifferentDimension_shouldNotBeEqual() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val l = EUnit("l", 1E-3, Dimension.of("volume"))
        val a = EProduct("a", kg)
        val b = EProduct("a", l)
        // when/then
        assertNotEquals(a,b)
    }

    @Test
    fun equals_whenDifferentName_shouldNotBEqual() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val a = EProduct("a", kg)
        val b = EProduct("b", kg)
        // when/then
        assertNotEquals(a,b)
    }
}
