package ch.kleis.lcaac.cli.cmd

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphCommandTest {
    @Test
    fun `simple graph`() {
        // given
        val cmd = GraphCommand()
        val argv = arrayOf("-s", "src/test/resources/main.zip", "main")

        // when
        val result = cmd.test(argv)

        // then
        assertEquals(
            "flowchart BT\n" +
                    "    p0[\"main\"]\n" +
                    "    p1[\"mill\"]\n" +
                    "    p2[\"wheat\"]\n" +
                    "    p1 --> p0\n" +
                    "    p2 --> p1\n",
            result.output
        )
    }
}