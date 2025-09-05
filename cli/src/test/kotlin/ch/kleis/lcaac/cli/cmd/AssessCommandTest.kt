package ch.kleis.lcaac.cli.cmd

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals

class AssessCommandTest {
    @Test
    fun `simple assessment`() {
        // given
        val cmd = AssessCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "main")

        // when
        val result = cmd.test(argv)

        // then
        assertEquals("product,amount,reference unit,grass,grass_unit\n" +
                    "bread,1.0,kg,2.0,kg\n",
            result.output
        )
    }

    @Test
    fun `assessment with data and src split`() {
        // given
        val cmd = AssessCommand()
        val argv = arrayOf(
            "-s", "src/test/resources/data-src-split/src",
            "-c", "src/test/resources/data-src-split/lcaac.yaml",
            "main")

        // when
        val result = cmd.test(argv)

        // then
        assertEquals("product,amount,reference unit,foo_fn,foo_fn_unit\n" +
                "main,1.0,u,6.0,u\n",
            result.output
        )
    }
}