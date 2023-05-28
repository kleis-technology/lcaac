package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.expression.EStringLiteral
import ch.kleis.lcaplugin.core.lang.expression.EStringRef
import ch.kleis.lcaplugin.core.lang.expression.StringExpression
import org.junit.Test
import kotlin.test.assertEquals


class StringExpressionReducerTest {
    @Test
    fun reduce_whenLiteral() {
        // given
        val expression = EStringLiteral("FR")
        val reducer = StringExpressionReducer(
            Register.empty<StringExpression>()
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        assertEquals(expression, actual)
    }

    @Test
    fun reduce_ref_whenFound() {
        // given
        val expression = EStringRef("geo")
        val reducer = StringExpressionReducer(
            Register.from(
                mapOf(
                    "geo" to EStringRef("geo2"),
                    "geo2" to EStringLiteral("FR"),
                )
            )
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EStringLiteral("FR")
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_ref_whenNotFound() {
        // given
        val expression = EStringRef("foo")
        val reducer = StringExpressionReducer(
            Register.from(
                mapOf(
                    "geo" to EStringRef("geo2"),
                    "geo2" to EStringLiteral("FR"),
                )
            )
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        assertEquals(expression, actual)
    }
}
