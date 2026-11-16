package ch.kleis.lcaac.grammar

import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class LcaLangParsingTest {
    private fun lcaFile(content: String) = Files.createTempFile("parsing", ".lca").also { Files.writeString(it, content) }

    @Test
    fun parseLcaFile_whenValid_shouldReturnContext() {
        // given
        val path = lcaFile(
            """
            process p {
                products {
                    1 kg x
                }
            }
            """.trimIndent()
        )

        // when
        val actual = parseLcaFile(path)

        // then
        assertEquals(1, actual.processDefinition().size)
    }

    @Test
    fun parseLcaFile_whenSyntaxError_shouldLocateIt() {
        // given
        val path = lcaFile(
            """
            process p {
                products {
                    1 kg x
                }
                inputs {
                    1 kg y from
                }
            }
            """.trimIndent()
        )

        // when
        val actual = assertThrows<SyntaxException> { parseLcaFile(path) }

        // then
        val error = actual.errors.first()
        assertEquals(path.toString(), error.source)
        assertEquals(7, error.line)
        assertEquals(5, error.column)
        assertEquals("$path:7:5: ${error.message}", error.toString())
    }

    @Test
    fun parseDataExpression_whenValid_shouldReturnContext() {
        // when
        val actual = parseDataExpression("2 kg * x")

        // then
        assertEquals("2kg*x", actual.text)
    }

    @Test
    fun parseDataExpression_whenTrailingInput_shouldThrow() {
        // when
        val actual = assertThrows<SyntaxException> { parseDataExpression("2 kg )", "<arg>") }

        // then
        assertEquals(listOf(SyntaxError("<arg>", 1, 6, "unexpected input ')'")), actual.errors)
    }
}
