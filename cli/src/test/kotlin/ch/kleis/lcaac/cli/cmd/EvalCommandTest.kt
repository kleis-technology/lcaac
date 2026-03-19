package ch.kleis.lcaac.cli.cmd

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals

class EvalCommandTest {
    @Test
    fun `addition of quantities`() {
        // given
        val cmd = EvalCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "1 kg + 2 kg")

        // when
        val result = cmd.test(argv)

        // then
        assertEquals("3.0 kg\n", result.output)
    }

    @Test
    fun `multiplication by scalar`() {
        // given
        val cmd = EvalCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "3 * 4 kg")

        // when
        val result = cmd.test(argv)

        // then
        assertEquals("12.0 kg\n", result.output)
    }

    @Test
    fun `global variable`() {
        // given
        val cmd = EvalCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "-G", "x=5 kg", "x")

        // when
        val result = cmd.test(argv)

        // then
        assertEquals("5.0 kg\n", result.output)
    }
}
