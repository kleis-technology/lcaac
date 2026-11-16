package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.explain.*
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorOptions
import ch.kleis.lcaac.core.lang.source.SourcePath
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.mockk
import kotlin.test.*

class ExplanationBuilderTest {
    private val ops = BasicOperations

    private fun explain(content: String, process: String, options: ExplainOptions = ExplainOptions()): Explanation<BasicNumber> {
        val symbolTable = Loader(ops).load(sequenceOf(LcaLangFixture.parser(content.trimIndent()).lcaFile()), listOf(LoaderOption.WITH_PRELUDE))
        val sourceOps = mockk<DataSourceOperations<BasicNumber>>()
        val trace = Evaluator(symbolTable, ops, sourceOps, EvaluatorOptions(recordDerivations = true))
            .trace(assertNotNull(symbolTable.getTemplate(process)))
        val analysis = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint()).run()
        return ExplanationBuilder<BasicNumber, BasicMatrix>(ops, options).build(trace, analysis)
    }

    private fun Explanation<BasicNumber>.process(name: String) = processes.single { it.process.name == name }

    private fun Explanation<BasicNumber>.allTablesMatchTheSolver() =
        processes.flatMap { it.contributions }.flatMap { it.tables }.all { it.matchesSolver }

    private val chain = """
        variables {
            loss = 10 percent
        }
        process main {
            products {
                1 kg bread
            }
            inputs {
                1 kg flour
            }
        }
        process mill {
            products {
                1 kg flour
            }
            inputs {
                2 kg * (1 u + loss) wheat
            }
        }
        process farm {
            products {
                1 kg wheat
            }
            emissions {
                0.5 kg co2(compartment = "air")
            }
        }
        substance co2 {
            name = "co2"
            type = Emission
            compartment = "air"
            reference_unit = kg
            impacts {
                1 kg_CO2_Eq climate_change
            }
        }
    """

    @Test
    fun chain_shouldNumberAndLinkTheProcesses() {
        val explanation = explain(chain, "main")

        assertEquals(listOf("main", "mill", "farm"), explanation.processes.map { it.process.name })
        assertEquals(listOf(0, 1, 2), explanation.processes.map { it.depth })
        assertEquals(listOf(2), explanation.process("farm").usedBy)
        assertEquals(listOf(Supplier.Explained(2, "mill")), explanation.entryPoint.suppliers)
        assertTrue(explanation.processes.all { it.loop.isEmpty() })
    }

    @Test
    fun chain_shouldGiveTheResultAndMatchTheSolver() {
        val explanation = explain(chain, "main")

        val impact = explanation.results.single().impacts.single()
        assertEquals("climate_change", impact.target.getShortName())
        assertEquals(1.1, impact.amount, 1e-9) // 2.2 kg wheat x 0.5 kg CO2
        assertTrue(explanation.allTablesMatchTheSolver())

        val mill = explanation.process("mill").contributions.single().tables.single()
        assertEquals(2.2, mill.rows.single().amount, 1e-9)
        assertEquals(0.5, mill.rows.single().factor, 1e-9)
    }

    @Test
    fun globals_shouldBeTheOnesUsed() {
        assertEquals(listOf(SourcePath.Global("loss")), explain(chain, "main").globals.map { it.path })
    }

    @Test
    fun targets_shouldFilterByShortName() {
        assertEquals(emptyList(), explain(chain, "main", ExplainOptions(targets = setOf("nope"))).targets)
        assertEquals(1, explain(chain, "main", ExplainOptions(targets = setOf("climate_change"))).targets.size)
    }

    @Test
    fun loop_shouldBeDetected() {
        val explanation = explain(
            """
            process main {
                products {
                    1 p widget
                }
                inputs {
                    2 kg steel
                }
            }
            process steel_mill {
                products {
                    1 kg steel
                }
                inputs {
                    3 kWh electricity
                }
                emissions {
                    1 kg co2(compartment = "air")
                }
            }
            process power_plant {
                products {
                    1 kWh electricity
                }
                inputs {
                    0.1 kg steel
                }
            }
            """, "main"
        )

        assertEquals(emptyList(), explanation.entryPoint.loop)
        assertEquals(listOf(2, 3), explanation.process("steel_mill").loop)
        assertEquals(listOf(2, 3), explanation.process("power_plant").loop)
        assertEquals(listOf(1, 3), explanation.process("steel_mill").usedBy)
        assertTrue(explanation.allTablesMatchTheSolver())
    }

    @Test
    fun maxDepth_shouldKeepProcessesReachedByAShortPath() {
        // c is at depth 2 through b, but at depth 1 from main directly
        val explanation = explain(
            """
            process main {
                products {
                    1 p a
                }
                inputs {
                    1 kg x
                    1 kg z
                }
            }
            process b {
                products {
                    1 kg x
                }
                inputs {
                    1 kg z
                }
            }
            process c {
                products {
                    1 kg z
                }
                inputs {
                    1 kg w
                }
            }
            process d {
                products {
                    1 kg w
                }
            }
            """, "main", ExplainOptions(maxDepth = 1)
        )

        assertEquals(listOf("main", "b", "c"), explanation.processes.map { it.process.name })
        assertEquals(listOf(Supplier.NotExplained("d")), explanation.process("c").suppliers)
        assertTrue(explanation.allTablesMatchTheSolver())
    }

    @Test
    fun allocation_shouldGiveTheShareOfEachProduct() {
        val explanation = explain(
            """
            process p {
                products {
                    1 p a allocate 1 kg
                    1 p b allocate 3 kg
                }
                emissions {
                    4 kg co2(compartment = "air")
                }
            }
            """, "p"
        )

        assertEquals(listOf(0.25, 0.75), explanation.entryPoint.contributions.map { it.share })
        assertEquals(listOf(1.0, 3.0), explanation.results.map { it.impacts.single().perUnit })
        assertTrue(explanation.allTablesMatchTheSolver())
    }

    @Test
    fun cachedProcess_shouldHaveNoDerivation() {
        val explanation = explain(
            """
            process main {
                products {
                    1 p a
                }
                inputs {
                    1 kg x
                }
            }
            @cached
            process cached {
                products {
                    1 kg x
                }
                emissions {
                    1 kg co2(compartment = "air")
                }
            }
            """, "main"
        )

        assertNotNull(explanation.entryPoint.derivation)
        assertNull(explanation.process("cached").derivation)
        assertTrue(explanation.allTablesMatchTheSolver())
    }
}
