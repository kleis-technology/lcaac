package ch.kleis.lcaplugin.core.lang

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class EnvironmentTest {
    @Test
    fun set_and_get() {
        // given
        val key = "abc.x"
        val a = EVar("a")
        val environment = Environment()

        // when
        environment[key] = a

        // then
        assertEquals(a, environment[key])
    }

    @Test
    fun set_whenDuplicate() {
        // given
        val key = "abc.x"
        val a = EVar("a")
        val b = EVar("b")
        val environment = Environment()

        // when
        try {
            environment[key] = a
            environment[key] = b
            fail("should have thrown IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // success
        }
    }
}
