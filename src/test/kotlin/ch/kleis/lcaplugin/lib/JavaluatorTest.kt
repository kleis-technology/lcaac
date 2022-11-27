package ch.kleis.lcaplugin.lib

import com.fathzer.soft.javaluator.DoubleEvaluator
import com.fathzer.soft.javaluator.StaticVariableSet
import junit.framework.TestCase.assertEquals
import org.junit.Test
import kotlin.math.sin

class JavaluatorTest {
    @Test
    fun test_simple() {
        // given
        val expression = "1 + (2 * sin(pi/2))"
        val evaluator = DoubleEvaluator()

        // when
        val actual = evaluator.evaluate(expression)

        // then
        assertEquals(actual, 3.0)
    }

    // https://sourceforge.net/p/javaluator/tickets/20/
    @Test
    fun test_format_double() {
        // given
        val expression = "1.23E-4"
        val evaluator = DoubleEvaluator(DoubleEvaluator.getDefaultParameters(), true)

        // when
        val actual = evaluator.evaluate(expression)

        // then
        assertEquals(actual, 1.23E-4)
    }

    @Test
    fun test_withVariables() {
        // given
        val expression = "sin(x)"
        val context = StaticVariableSet<Double>()
        context.set("x", 2.0)
        val evaluator = DoubleEvaluator()

        // when
        val actual = evaluator.evaluate(expression, context)

        // then
        assertEquals(actual, sin(2.0))
    }
}
