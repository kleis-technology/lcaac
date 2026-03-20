package ch.kleis.lcaac.cli.cmd

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TraceCommandTest {
    @Test
    fun `simple trace, csv`() {
        val cmd = TraceCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "-o", "csv", "main")
        val result = cmd.test(argv)
        assertEquals(
            "depth,d_amount,d_unit,d_product,alloc,name,a,b,c,amount,unit,grass,grass_unit\n" +
                "0,1.0,kg,bread,1.0,bread,main,{},{},1.0,kg,2.0,kg\n" +
                "1,1.0,kg,bread,1.0,flour,mill,{},{},1.0,kg,2.0,kg\n" +
                "2,1.0,kg,bread,1.0,wheat,wheat,{},{},2.0,kg,2.0,kg\n",
            result.output
        )
    }

    @Test
    fun `simple trace, text`() {
        val cmd = TraceCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "-o", "text", "main")
        val result = cmd.test(argv)
        val lines = result.output.lines().filter { it.isNotEmpty() }
        assertEquals(5, lines.size)  // header, separator, 3 data rows
        assertTrue(lines[0].startsWith("depth  d_amount  d_unit"))
        assertTrue(lines[1].startsWith("-----  --------  ------"))
        assertTrue(lines[2].contains("bread"))
        assertTrue(lines[3].contains("flour"))
        assertTrue(lines[4].contains("wheat"))
    }

    @Test
    fun `simple trace, json`() {
        val cmd = TraceCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "-o", "json", "main")
        val result = cmd.test(argv)
        assertTrue(result.output.startsWith("[\n{"))
        assertTrue(result.output.contains("\"trace\": ["))
        assertTrue(result.output.contains("\"name\": \"bread\""))
        assertTrue(result.output.endsWith("}\n]\n"))
    }

    @Test
    fun `trace with indicators filter`() {
        val cmd = TraceCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "-o", "csv", "-i", "grass", "main")
        val result = cmd.test(argv)
        assertEquals(
            "depth,d_amount,d_unit,d_product,alloc,name,a,b,c,amount,unit,grass,grass_unit\n" +
                "0,1.0,kg,bread,1.0,bread,main,{},{},1.0,kg,2.0,kg\n" +
                "1,1.0,kg,bread,1.0,flour,mill,{},{},1.0,kg,2.0,kg\n" +
                "2,1.0,kg,bread,1.0,wheat,wheat,{},{},2.0,kg,2.0,kg\n",
            result.output
        )
    }

    @Test
    fun `trace with data and src split`() {
        val cmd = TraceCommand()
        val argv = arrayOf(
            "-s", "src/test/resources/data-src-split/src",
            "-c", "src/test/resources/data-src-split/lcaac.yaml",
            "-o", "csv",
            "main"
        )
        val result = cmd.test(argv)
        assertEquals(
            "depth,d_amount,d_unit,d_product,alloc,name,a,b,c,amount,unit,foo_fn,foo_fn_unit\n" +
                "0,1.0,u,main,1.0,main,main,{},{},1.0,u,6.0,u\n",
            result.output
        )
    }
}
