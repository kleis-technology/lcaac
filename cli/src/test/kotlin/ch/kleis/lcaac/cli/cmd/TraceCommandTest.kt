package ch.kleis.lcaac.cli.cmd

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals

class TraceCommandTest {
    @Test
    fun `simple trace`() {
        // given
        val cmd = TraceCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "main")

        // when
        val result = cmd.test(argv)

        // then
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
        // given
        val cmd = TraceCommand()
        val argv = arrayOf(
            "-s", "src/test/resources/data-src-split/src",
            "-c", "src/test/resources/data-src-split/lcaac.yaml",
            "main")

        // when
        val result = cmd.test(argv)

        // then
        assertEquals("depth,d_amount,d_unit,d_product,alloc,name,a,b,c,amount,unit,foo_fn,foo_fn_unit\n" +
                "0,1.0,u,main,1.0,main,main,{},{},1.0,u,6.0,u\n",
            result.output
        )
    }
}