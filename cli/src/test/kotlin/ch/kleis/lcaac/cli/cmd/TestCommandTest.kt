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

    @Test
    fun `evaluation errors are located`() {
        // given
        val root = kotlin.io.path.createTempDirectory().toFile()
        val file = java.io.File(root, "main.lca")
        file.writeText(
            """
            test bad_variable {
                variables {
                    x = 1 kg + 1 kWh
                }
                given {
                    x steel
                }
                assert {
                    steel between 0 kg and 10 kg
                }
            }
            """.trimIndent()
        )
        val cmd = TestCommand()

        // when
        val result = cmd.test(arrayOf("-s", root.path))

        // then
        assertEquals(1, result.statusCode)
        assertEquals(
            """
            ❌  bad_variable[0] incompatible dimensions: mass vs energy in left=kg and right=kWh
                 in variable 'x' (${file.path}:3:9)
                 in input 'steel' (${file.path}:6:9)
                 in test 'bad_variable' (${file.path}:1:1)
            Run 1 tests, 0 passed, 1 failed

            """.trimIndent(),
            result.output,
        )
    }

    @Test
    fun `failing assertions are located`() {
        // given
        val root = kotlin.io.path.createTempDirectory().toFile()
        val file = java.io.File(root, "main.lca")
        file.writeText(
            """
            process steel_mill {
                products {
                    1 kg steel
                }
            }

            test too_much {
                given {
                    12 kg steel
                }
                assert {
                    steel between 0 kg and 10 kg
                }
            }
            """.trimIndent()
        )
        val cmd = TestCommand()

        // when
        val result = cmd.test(arrayOf("-s", root.path))

        // then
        assertEquals(
            """
            ❌  too_much[0] steel = 12.0 kg is not in between 0.0 kg and 10.0 kg
                 in assertion on 'steel' (${file.path}:12:9)
            Run 1 tests, 0 passed, 1 failed

            """.trimIndent(),
            result.output,
        )
    }
}
