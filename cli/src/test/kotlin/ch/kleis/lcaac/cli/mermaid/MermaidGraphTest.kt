package ch.kleis.lcaac.cli.mermaid

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import io.mockk.mockk
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MermaidGraphTest {

    // region helpers

    private val sourceOps = mockk<DataSourceOperations<BasicNumber>>()

    private fun load(content: String): SymbolTable<BasicNumber> {
        val lexer = LcaLangLexer(CharStreams.fromStream(content.byteInputStream()))
        val tokens = CommonTokenStream(lexer)
        val parser = LcaLangParser(tokens)
        val file = parser.lcaFile()
        return Loader(BasicOperations).load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
    }

    private fun trace(content: String, processName: String, labels: Map<String, String> = emptyMap()) =
        load(content).let { symbolTable ->
            val template = symbolTable.getTemplate(processName, labels)!!
            Evaluator(symbolTable, BasicOperations, sourceOps).trace(template)
        }

    // endregion

    @Test
    fun `single process, no options`() {
        // given
        val content = """
            process main {
                products { 1 kWh electricity }
            }
        """.trimIndent()
        val trace = trace(content, "main")

        // when
        val result = MermaidGraph(trace).render()

        // then
        val expected = """
            flowchart BT
                classDef invisible fill:none,stroke:none
                ep0[ ]:::invisible
                prod0["main"]
                prod0 -->|"1.0 kWh electricity"| ep0

        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `technosphere chain, no options`() {
        // given
        val content = """
            process main {
                products { 1 kWh electricity }
                inputs { 1 kg flour from mill }
            }
            process mill {
                products { 1 kg flour }
                inputs { 1 kg wheat }
            }
        """.trimIndent()
        val trace = trace(content, "main")

        // when
        val result = MermaidGraph(trace).render()

        // then
        val expected = """
            flowchart BT
                classDef invisible fill:none,stroke:none
                ep0[ ]:::invisible
                prod0["main"]
                prod1["mill"]
                dang0["wheat"]
                prod0 -->|"1.0 kWh electricity"| ep0
                prod1 -->|"1.0 kg flour"| prod0
                dang0 -->|"1.0 kg wheat"| prod1

        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `HIDE_PRODUCTS on technosphere edge`() {
        // given
        val content = """
            process main {
                products { 1 kWh electricity }
                inputs { 1 kg flour from mill }
            }
            process mill {
                products { 1 kg flour }
            }
        """.trimIndent()
        val trace = trace(content, "main")

        // when
        val result = MermaidGraph(trace, setOf(MermaidGraphOption.HIDE_PRODUCTS)).render()

        // then
        val expected = """
            flowchart BT
                classDef invisible fill:none,stroke:none
                ep0[ ]:::invisible
                prod0["main"]
                prod1["mill"]
                prod0 -->|"1.0 kWh"| ep0
                prod1 -->|"1.0 kg"| prod0

        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `HIDE_QUANTITIES on technosphere edge`() {
        // given
        val content = """
            process main {
                products { 1 kWh electricity }
                inputs { 1 kg flour from mill }
            }
            process mill {
                products { 1 kg flour }
            }
        """.trimIndent()
        val trace = trace(content, "main")

        // when
        val result = MermaidGraph(trace, setOf(MermaidGraphOption.HIDE_QUANTITIES)).render()

        // then
        val expected = """
            flowchart BT
                classDef invisible fill:none,stroke:none
                ep0[ ]:::invisible
                prod0["main"]
                prod1["mill"]
                prod0 -->|"electricity"| ep0
                prod1 -->|"flour"| prod0

        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `SHOW_BIOSPHERE with substance characterization`() {
        // given
        val content = """
            process main {
                products { 1 kWh electricity }
                emissions { 1 kg CO2 (compartment="air") }
            }
            substance CO2 {
                name = "CO2"
                type = Emission
                compartment = "air"
                reference_unit = kg
                impacts {
                    1 kg climate_change
                }
            }
        """.trimIndent()
        val trace = trace(content, "main")

        // when
        val result = MermaidGraph(trace, setOf(MermaidGraphOption.SHOW_BIOSPHERE)).render()

        // then
        val expected = """
            flowchart BT
                classDef invisible fill:none,stroke:none
                ep0[ ]:::invisible
                prod0["main"]
                ind0["climate_change"]
                prod0 -->|"1.0 kWh electricity"| ep0
                ind0 -->|"1.0 kg climate_change"| prod0

        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `SHOW_BIOSPHERE with dangling substance`() {
        // given
        val content = """
            process main {
                products { 1 kWh electricity }
                emissions { 1 kg CO2 (compartment="air") }
            }
        """.trimIndent()
        val trace = trace(content, "main")

        // when
        val result = MermaidGraph(trace, setOf(MermaidGraphOption.SHOW_BIOSPHERE)).render()

        // then
        val expected = """
            flowchart BT
                classDef invisible fill:none,stroke:none
                ep0[ ]:::invisible
                prod0["main"]
                sub0["[Emission] CO2(air)"]
                prod0 -->|"1.0 kWh electricity"| ep0
                sub0 -->|"1.0 kg [Emission] CO2(air)"| prod0

        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `SHOW_IMPACTS with direct process impact`() {
        // given
        val content = """
            process main {
                products { 1 kWh electricity }
                impacts { 1 kg climate_change }
            }
        """.trimIndent()
        val trace = trace(content, "main")

        // when
        val result = MermaidGraph(trace, setOf(MermaidGraphOption.SHOW_IMPACTS)).render()

        // then
        val expected = """
            flowchart BT
                classDef invisible fill:none,stroke:none
                ep0[ ]:::invisible
                prod0["main"]
                ind0["climate_change"]
                prod0 -->|"1.0 kWh electricity"| ep0
                ind0 -->|"1.0 kg climate_change"| prod0

        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun `nodeLabel includes process arguments`() {
        // given
        val content = """
            process main {
                params { x = 1 kg }
                products { 1 kWh electricity }
                inputs { 1 kg flour from mill(x = 2 kg) }
            }
            process mill {
                labels { region = "EU" }
                params { x = 1 kg }
                products { x flour }
            }
        """.trimIndent()
        val trace = trace(content, "main")

        // when
        val result = MermaidGraph(trace).render()

        // then
        val expected = """
            flowchart BT
                classDef invisible fill:none,stroke:none
                ep0[ ]:::invisible
                prod0["main\n{x: 1.0 kg}"]
                dang0["flour from mill{}{x=2.0 kg}"]
                prod0 -->|"1.0 kWh electricity"| ep0
                dang0 -->|"1.0 kg flour"| prod0

        """.trimIndent()
        assertEquals(expected, result)
    }
}
