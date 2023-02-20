package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.ELet
import ch.kleis.lcaplugin.core.lang.ETemplate
import ch.kleis.lcaplugin.core.lang.EVar
import org.junit.Assert.assertEquals
import org.junit.Test


class HelperTest {
    @Test
    fun freeVariables_var_free() {
        // given
        val expression = EVar("x")
        val boundedVars = emptySet<String>()
        val helper = Helper()

        // when
        val actual = helper.freeVariables(boundedVars, expression)

        // then
        val expected = setOf("x")
        assertEquals(expected, actual)
    }

    @Test
    fun freeVariables_var_bounded() {
        // given
        val expression = EVar("x")
        val boundedVars = setOf("x")
        val helper = Helper()

        // when
        val actual = helper.freeVariables(boundedVars, expression)

        // then
        val expected = emptySet<String>()
        assertEquals(expected, actual)
    }

    @Test
    fun freeVariables_let_bounded() {
        // given
        val expression = ELet(mapOf(
            Pair("x", EVar("a"))
        ), EVar("x"))
        val boundedVars = emptySet<String>()
        val helper = Helper()

        // when
        val actual = helper.freeVariables(boundedVars, expression)

        // then
        val expected = setOf("a")
        assertEquals(expected, actual)
    }

    @Test
    fun freeVariables_let_chaining() {
        // given
        val expression = ELet(mapOf(
            Pair("x", EVar("a")),
            Pair("y", EVar("x")),
        ), EVar("y"))
        val boundedVars = emptySet<String>()
        val helper = Helper()

        // when
        val actual = helper.freeVariables(boundedVars, expression)

        // then
        val expected = setOf("a")
        assertEquals(expected, actual)
    }

    @Test
    fun freeVariables_template() {
        // given
        val expression = ETemplate(mapOf(
            Pair("x", null)
        ), EVar("a"))
        val boundedVars = emptySet<String>()
        val helper = Helper()

        // when
        val actual = helper.freeVariables(boundedVars, expression)

        // then
        val expected = setOf("a")
        assertEquals(expected, actual)
    }

    @Test
    fun freeVariables_template_withDefaultValue() {
        // given
        val expression = ETemplate(mapOf(
            Pair("x", EVar("a"))
        ), EVar("x"))
        val boundedVars = emptySet<String>()
        val helper = Helper()

        // when
        val actual = helper.freeVariables(boundedVars, expression)

        // then
        val expected = setOf("a")
        assertEquals(expected, actual)
    }

    @Test
    fun freeVariables_template_combined() {
        // given
        val expression = ETemplate(mapOf(
            Pair("x", EVar("a"))
        ), EVar("b"))
        val boundedVars = emptySet<String>()
        val helper = Helper()

        // when
        val actual = helper.freeVariables(boundedVars, expression)

        // then
        val expected = setOf("a", "b")
        assertEquals(expected, actual)
    }

    @Test
    fun rename_var() {
        // given
        val expression = EVar("x")
        val helper = Helper()

        // when
        val actual = helper.rename("x", "y", expression)

        // then
        val expected = EVar("y")
        assertEquals(expected, actual)
    }

    @Test
    fun rename_template_withDefaultValue() {
        // given
        val expression = ETemplate(
            mapOf(
                Pair("a", EVar("x")),
            ),
            EVar("z")
        )
        val helper = Helper()

        // when
        val actual = helper.rename("x", "y", expression)

        // then
        val expected = ETemplate(
            mapOf(
                Pair("a", EVar("y")),
            ),
            EVar("z")
        )
        assertEquals(expected, actual)
    }

    @Test
    fun rename_template_inBody() {
        // given
        val expression = ETemplate(
            mapOf(
                Pair("a", EVar("z")),
            ),
            EVar("x")
        )
        val helper = Helper()

        // when
        val actual = helper.rename("x", "y", expression)

        // then
        val expected = ETemplate(
            mapOf(
                Pair("a", EVar("z")),
            ),
            EVar("y")
        )
        assertEquals(expected, actual)
    }

    @Test
    fun newName() {
        // given
        val binder = "x"
        val others = setOf("x0", "x1", "y")
        val helper = Helper()

        // when
        val actual = helper.newName(binder, others)

        // then
        val expected = "x2"
        assertEquals(expected, actual)
    }
}
