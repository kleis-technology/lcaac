package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class EnvironmentTest {
    @Test
    fun set_and_get() {
        // given
        val key = "abc.x"
        val a = EQuantityRef("a")
        val environment = Environment.empty<QuantityExpression>()

        // when
        environment[key] = a

        // then
        assertEquals(a, environment[key])
    }

    @Test
    fun set_whenDuplicate() {
        // given
        val key = "abc.x"
        val a = EQuantityRef("a")
        val b = EQuantityRef("b")
        val environment = Environment.empty<QuantityExpression>()

        // when
        try {
            environment[key] = a
            environment[key] = b
            fail("should have thrown IllegalArgumentException")
        } catch (e: EvaluatorException) {
            assertEquals("reference $key already bound: $key = $a", e.message)
        }
    }
}
