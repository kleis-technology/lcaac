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
                    "    classDef invisible fill:none,stroke:none\n" +
                    "    ep0[ ]:::invisible\n" +
                    "    prod0[\"main\"]\n" +
                    "    prod1[\"mill\"]\n" +
                    "    prod2[\"wheat\"]\n" +
                    "    dang0[\"grass\"]\n" +
                    "    prod0 -->|\"bread\"| ep0\n" +
                    "    prod1 -->|\"flour\"| prod0\n" +
                    "    prod2 -->|\"wheat\"| prod1\n" +
                    "    dang0 -->|\"grass\"| prod2\n",
            result.output
        )
    }
}