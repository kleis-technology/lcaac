package ch.kleis.lcaac.cli

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.grammar.LoaderException
import ch.kleis.lcaac.grammar.SyntaxError
import ch.kleis.lcaac.grammar.SyntaxException
import com.github.ajalt.clikt.core.CliktError
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class MainTest {
    @Test
    fun withCleanErrors_whenNoError_shouldReturnResult() {
        assertEquals(42, withCleanErrors { 42 })
    }

    @Test
    fun withCleanErrors_whenUserError_shouldThrowCliktErrorWithMessage() {
        val errors = listOf(
            SyntaxException(listOf(SyntaxError("src/main.lca", 7, 5, "missing ID at '}'"))),
            LoaderException("Duplicate process [p] defined"),
            EvaluatorException("incompatible dimensions"),
        )
        errors.forEach { error ->
            val actual = assertThrows<CliktError> { withCleanErrors { throw error } }
            assertEquals(error.message, actual.message)
            assertEquals(1, actual.statusCode)
        }
    }

    @Test
    fun withCleanErrors_whenUnexpectedError_shouldRethrowIt() {
        assertThrows<IllegalStateException> { withCleanErrors { throw IllegalStateException("bug") } }
    }
}
