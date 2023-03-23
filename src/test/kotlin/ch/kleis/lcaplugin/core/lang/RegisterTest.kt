package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.EQuantityRef
import ch.kleis.lcaplugin.core.lang.expression.QuantityExpression
import org.junit.Assert.*
import org.junit.Test

class RegisterTest {
    @Test
    fun set_and_get() {
        // given
        val key = "abc.x"
        val a = EQuantityRef("a")
        val register = Register.empty<QuantityExpression>()

        // when
        register[key] = a

        // then
        assertEquals(a, register[key])
    }

    @Test
    fun set_whenDuplicate() {
        // given
        val key = "abc.x"
        val a = EQuantityRef("a")
        val b = EQuantityRef("b")
        val register = Register.empty<QuantityExpression>()

        // when
        try {
            register[key] = a
            register[key] = b
            fail("should have thrown IllegalArgumentException")
        } catch (e: EvaluatorException) {
            assertEquals("reference $key already bound: $key = $a", e.message)
        }
    }

    @Test
    fun equals_whenEquals_thenTrue() {
        // given
        val r1 = Register("a" to 1.0, "b" to 2.0)
        val r2 = Register("a" to 1.0, "b" to 2.0)

        // then
        assertEquals(r1, r2)
    }

    @Test
    fun equals_whenSameKeysDifferentValue_thenFalse() {
        // given
        val r1 = Register("a" to 1.0, "b" to 2.0)
        val r2 = Register("a" to 1.0, "b" to 3.0)

        // then
        assertNotEquals(r1, r2)
    }

    @Test
    fun equals_whenDifferentKeys_thenFalse() {
        // given
        val r1 = Register("a" to 1.0, "b" to 2.0)
        val r2 = Register("a" to 1.0, "c" to 2.0)

        // then
        assertNotEquals(r1, r2)
    }
}
