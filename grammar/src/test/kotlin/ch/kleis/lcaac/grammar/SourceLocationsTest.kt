package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.expression.BlockExpression
import ch.kleis.lcaac.core.lang.expression.EBlockForEach
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.source.*
import ch.kleis.lcaac.core.math.basic.BasicOperations
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SourceLocationsTest {
    // Line numbers are used in the assertions: keep the content as is
    private val content = """
        variables {
            grid_loss = 10 percent
        }

        params {
            efficiency = 80 percent
        }

        unit widget_unit {
            symbol = "wu"
            dimension = "widget"
        }

        datasource parts {
            location = "parts.csv"
            schema {
                mass = 1 kg
            }
        }

        @cached
        process assembly {
            labels {
                geo = "FR"
            }
            params {
                machine = "m1"
            }
            variables {
                energy = 2 kWh
            }
            products {
                1 widget_unit widget
            }
            inputs {
                energy electricity
            }
            land_use {
                1 m2 forest
            }
            inputs {
                for_each part from parts {
                    variables {
                        wrap = 10 percent * part.mass
                    }
                    wrap packaging
                }
            }
            emissions {
                1 kg co2
            }
            resources {
                1 kg water
            }
            impacts {
                1 u climate_change
            }
        }
    """.trimIndent()

    private val path = Files.createTempFile("source-locations", ".lca").also { Files.writeString(it, content) }
    private val symbolTable = Loader(BasicOperations).load(sequenceOf(parseLcaFile(path)))
    private val sourceMap = symbolTable.sourceMap
    private val assembly = ProcessKey("assembly", mapOf("geo" to "FR"))

    private fun at(line: Int, column: Int) = SourceLocation(path.toString(), line, column)
    private fun exchange(section: Section, vararg indices: Int) =
        SourcePath.Exchange(assembly, BlockPath(section, indices.toList()))

    @Test
    fun globals_unitsAndDataSources() {
        assertEquals(at(2, 5), sourceMap[SourcePath.Global("grid_loss")])
        assertEquals(at(6, 5), sourceMap[SourcePath.Global("efficiency")])
        assertEquals(at(9, 1), sourceMap[SourcePath.Global("widget_unit")])
        assertEquals(at(14, 1), sourceMap[SourcePath.DataSource("parts")])
    }

    @Test
    fun process_shouldPointAtProcessKeywordAfterAnnotations() {
        assertEquals(at(22, 1), sourceMap[SourcePath.Process(assembly)])
    }

    @Test
    fun parametersAndVariables() {
        assertEquals(at(27, 9), sourceMap[SourcePath.Parameter(assembly, "machine")])
        assertEquals(at(30, 9), sourceMap[SourcePath.Variable(assembly, "energy")])
    }

    @Test
    fun products() {
        assertEquals(at(33, 9), sourceMap[exchange(Section.PRODUCTS, 0)])
    }

    @Test
    fun inputs_shouldNumberSeveralBlocksAsOneList() {
        assertEquals(at(36, 9), sourceMap[exchange(Section.INPUTS, 0)])
        assertEquals(at(42, 9), sourceMap[exchange(Section.INPUTS, 1)])
    }

    @Test
    fun forEach_bodyAndVariables() {
        assertEquals(at(46, 13), sourceMap[exchange(Section.INPUTS, 1, 0)])
        assertEquals(
            at(44, 17),
            sourceMap[SourcePath.Variable(assembly, "wrap", BlockPath(Section.INPUTS, listOf(1)))],
        )
    }

    @Test
    fun biosphere_shouldListEmissionsThenResourcesThenLandUse() {
        assertEquals(at(50, 9), sourceMap[exchange(Section.BIOSPHERE, 0)])
        assertEquals(at(53, 9), sourceMap[exchange(Section.BIOSPHERE, 1)])
        assertEquals(at(39, 9), sourceMap[exchange(Section.BIOSPHERE, 2)])
    }

    @Test
    fun impacts() {
        assertEquals(at(56, 9), sourceMap[exchange(Section.IMPACTS, 0)])
    }

    @Test
    fun exchangePaths_shouldMatchTheProcessExpression() {
        // given
        val body = assertNotNull(symbolTable.getTemplate("assembly", mapOf("geo" to "FR"))).body
        val paths = sourceMap.paths().filterIsInstance<SourcePath.Exchange>()

        // then: every path points to a block, and every block has a path
        paths.forEach { assertNotNull(body.blockAt(it.block), "no block at $it") }
        val nbBlocks = listOf(body.inputs, body.biosphere, body.impacts).sumOf { countBlocks(it) } + body.products.size
        assertEquals(nbBlocks, paths.size)
    }

    private fun countBlocks(blocks: List<BlockExpression<*, *>>): Int = blocks.sumOf {
        1 + if (it is EBlockForEach) countBlocks(it.body) else 0
    }
}
