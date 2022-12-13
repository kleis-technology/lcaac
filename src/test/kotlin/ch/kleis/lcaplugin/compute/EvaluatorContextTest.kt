package ch.kleis.lcaplugin.compute

import junit.framework.TestCase.assertEquals
import org.junit.Test


internal class EvaluatorContextTest {
    @Test
    fun getDouble() {
        // given
        val s = "12"
        val context = EvaluatorContext()

        // when
        val actual = context.get(s)

        // then
        assertEquals(12.0, actual)
    }

    @Test
    fun getVariable() {
        // given
        val s = "A"
        val context = EvaluatorContext()
        context.put(s, 12.0)

        // when
        val actual = context.get(s)

        // then
        assertEquals(12.0, actual)
    }

}
