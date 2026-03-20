package ch.kleis.lcaac.cli.cmd

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssessCommandTest {
    @Test
    fun `simple assessment, csv`() {
        val cmd = AssessCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "-o", "csv", "main")
        val result = cmd.test(argv)
        assertEquals(
            "product,amount,reference unit,grass,grass_unit\n" +
                "bread,1.0,kg,2.0,kg\n",
            result.output
        )
    }

    @Test
    fun `simple assessment, text`() {
        val cmd = AssessCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "-o", "text", "main")
        val result = cmd.test(argv)
        assertEquals(
            "product  amount  reference unit  grass  grass_unit\n" +
                "-------  ------  --------------  -----  ----------\n" +
                "bread    1.0     kg              2.0    kg\n",
            result.output
        )
    }

    @Test
    fun `simple assessment, json`() {
        val cmd = AssessCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "-o", "json", "main")
        val result = cmd.test(argv)
        assertTrue(result.output.startsWith("[\n{"))
        assertTrue(result.output.contains("\"product\": \"bread\""))
        assertTrue(result.output.contains("\"reference_unit\": \"kg\""))
        assertTrue(result.output.contains("\"grass\": {\"amount\": 2.0, \"unit\": \"kg\"}"))
        assertTrue(result.output.endsWith("}\n]\n"))
    }

    @Test
    fun `assessment with indicators filter`() {
        val cmd = AssessCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "-o", "csv", "-i", "grass", "main")
        val result = cmd.test(argv)
        assertEquals(
            "product,amount,reference unit,grass,grass_unit\n" +
                "bread,1.0,kg,2.0,kg\n",
            result.output
        )
    }

    @Test
    fun `assessment with data and src split`() {
        val cmd = AssessCommand()
        val argv = arrayOf(
            "-s", "src/test/resources/data-src-split/src",
            "-c", "src/test/resources/data-src-split/lcaac.yaml",
            "-o", "csv",
            "main"
        )
        val result = cmd.test(argv)
        assertEquals(
            "product,amount,reference unit,foo_fn,foo_fn_unit\n" +
                "main,1.0,u,6.0,u\n",
            result.output
        )
    }
}
