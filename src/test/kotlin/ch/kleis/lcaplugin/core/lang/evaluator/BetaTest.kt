package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.EBlock
import ch.kleis.lcaplugin.core.lang.ELet
import ch.kleis.lcaplugin.core.lang.ETemplate
import ch.kleis.lcaplugin.core.lang.EVar
import org.junit.Assert.assertEquals
import org.junit.Test


class BetaTest {
    @Test
    fun substitute_template_noConflict() {
        // given
        val template = ETemplate(
            mapOf(
                Pair("x", null),
            ),
            EVar("y")
        )
        val beta = Beta()

        // when
        val actual = beta.substitute("y", EVar("a"), template)

        // then
        val expected = ETemplate(
            mapOf(
                Pair("x", null),
            ),
            EVar("a"),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun substitute_template_shadow() {
        // given
        val template = ETemplate(
            mapOf(
                Pair("x", null),
            ),
            EVar("x")
        )
        val beta = Beta()

        // when
        val actual = beta.substitute("x", EVar("a"), template)

        // then
        assertEquals(template, actual)
    }

    @Test
    fun substitute_template_withMatchingDefaultValue() {
        // given
        val template = ETemplate(
            mapOf(
                Pair("x", EVar("y")),
            ),
            EVar("x")
        )
        val beta = Beta()

        // when
        val actual = beta.substitute("y", EVar("a"), template)

        // then
        val expected = ETemplate(
            mapOf(
                Pair("x", EVar("a")),
            ),
            EVar("x")
        )
        assertEquals(expected, actual)
    }

    @Test
    fun substitute_whenParamOccursAsFreeVariableOfBinderValue() {
        // given
        val template = ETemplate(
            mapOf(
                Pair("x", null)
            ),
            EBlock(
                listOf(
                    EVar("x"),
                    EVar("y"),
                )
            )
        )
        val beta = Beta()

        // when
        val actual = beta.substitute("y", EVar("x"), template)

        // then
        val expected = ETemplate(
            mapOf(
                Pair("x0", null)
            ),
            EBlock(
                listOf(
                    EVar("x0"),
                    EVar("x"),
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun substitute_let_inLocals() {
        // given
        val expression = ELet(
            mapOf(
                Pair("x", EVar("a"))
            ), EBlock(
                listOf(
                    EVar("x"),
                    EVar("y"),
                )
            )
        )
        val beta = Beta()

        // when
        val actual = beta.substitute("a", EVar("b"), expression)

        // then
        val expected = ELet(
            mapOf(
                Pair("x", EVar("b"))
            ), EBlock(
                listOf(
                    EVar("x"),
                    EVar("y"),
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun substitute_let_inBody() {
        // given
        val expression = ELet(
            mapOf(
                Pair("x", EVar("a"))
            ), EBlock(
                listOf(
                    EVar("x"),
                    EVar("y"),
                )
            )
        )
        val beta = Beta()

        // when
        val actual = beta.substitute("y", EVar("b"), expression)

        // then
        val expected = ELet(
            mapOf(
                Pair("x", EVar("a"))
            ), EBlock(
                listOf(
                    EVar("x"),
                    EVar("b"),
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun substitute_let_shadow() {
        // given
        val expression = ELet(
            mapOf(
                Pair("x", EVar("a"))
            ), EBlock(
                listOf(
                    EVar("x"),
                    EVar("y"),
                )
            )
        )
        val beta = Beta()

        // when
        val actual = beta.substitute("x", EVar("b"), expression)

        // then
        assertEquals(expression, actual)
    }


    @Test
    fun substitute_let_whenLocalOccursAsFreeVariableOfValue() {
        // given
        val expression = ELet(
            mapOf(
                Pair("x", EVar("a"))
            ),
            EBlock(
                listOf(
                    EVar("x"),
                    EVar("y"),
                )
            )
        )
        val beta = Beta()

        // when
        val actual = beta.substitute("y", EVar("x"), expression)

        // then
        val expected = ELet(
            mapOf(
                Pair("x0", EVar("a"))
            ),
            EBlock(
                listOf(
                    EVar("x0"),
                    EVar("x"),
                )
            )
        )
        assertEquals(expected, actual)
    }
}
