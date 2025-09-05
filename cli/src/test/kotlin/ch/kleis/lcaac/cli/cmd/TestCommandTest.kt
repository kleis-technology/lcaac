package ch.kleis.lcaac.cli.cmd

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals

class TestCommandTest {
    @Test
    fun `no test`() {
        // given
        val cmd = TestCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "main")

        // when
        val result = cmd.test(argv)

        // then
        assertEquals("Run 0 tests, 0 passed, 0 failed\n", result.output)
    }

    @Test
    fun `test simple project`() {
        // given
        val cmd = TestCommand()
        val argv = arrayOf("-s", "src/test/resources/simple-project", "bake")

        // when
        val result = cmd.test(argv)

        // then
        assertEquals("Run 1 tests, 1 passed, 0 failed\n", result.output)
    }

    @Test
    fun `test with data and src split`() {
        // given
        val cmd = TestCommand()
        val argv = arrayOf(
            "-s", "src/test/resources/data-src-split/src",
            "-c", "src/test/resources/data-src-split/lcaac.yaml",
            "main")

        // when
        val result = cmd.test(argv)

        // then
        assertEquals("Run 1 tests, 1 passed, 0 failed\n", result.output)
    }
}