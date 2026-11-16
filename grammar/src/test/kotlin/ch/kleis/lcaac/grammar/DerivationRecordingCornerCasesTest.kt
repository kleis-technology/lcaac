package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorOptions
import ch.kleis.lcaac.core.lang.evaluator.derivation.ReadKind
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.source.BlockPath
import ch.kleis.lcaac.core.lang.source.Section
import ch.kleis.lcaac.core.lang.source.SourcePath
import ch.kleis.lcaac.core.lang.value.ProcessValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.every
import io.mockk.mockk
import kotlin.test.*

class DerivationRecordingCornerCasesTest {
    private val content = """
        datasource parts {
            location = "parts.csv"
            schema {
                id = "p1"
                mass = 1 kg
            }
        }

        datasource empty {
            location = "empty.csv"
            schema {
                id = "p1"
                mass = 1 kg
            }
        }

        process main {
            params {
                key = "p1"
            }
            variables {
                def = default_record from parts
                unused = 42 kg
                with_key = lookup parts match id = key
                broken_unused = 1 kg + 1 kWh
            }
            products {
                1 p widget
            }
            inputs {
                sum(parts, mass) copper
                def.mass zinc
                1 kg steel from mill(grade = "A")
                1 kg steel from mill(grade = "B")
                for_each row from empty {
                    row.mass lead
                }
                for_each part from parts {
                    variables {
                        unused_in_row = part.mass
                    }
                    1 kg tin
                }
            }
        }

        process mill {
            params {
                grade = "A"
            }
            products {
                1 kg steel
            }
            inputs {
                1 kg ore from cached_mine
            }
        }

        @cached
        process cached_mine {
            products {
                1 kg ore
            }
        }
    """.trimIndent()

    private fun kg(amount: Double) =
        EQuantityScale(BasicNumber(amount), EUnitLiteral(UnitSymbol.of("kg"), 1.0, Dimension.of("mass")))

    private val sourceOps = mockk<DataSourceOperations<BasicNumber>>().also {
        every { it.getAll(match { source -> source.config.name == "empty" }) } returns emptySequence()
        every { it.getAll(match { source -> source.config.name == "parts" }) } returns
            sequenceOf(ERecord(mapOf("id" to EStringLiteral("p1"), "mass" to kg(2.0))))
        every { it.sumProduct(any(), any()) } returns kg(2.0)
        every { it.getFirst(any()) } returns ERecord(mapOf("id" to EStringLiteral("p1"), "mass" to kg(2.0)))
    }
    private val symbolTable = Loader(BasicOperations)
        .load(sequenceOf(LcaLangFixture.parser(content).lcaFile()), listOf(LoaderOption.WITH_PRELUDE))
    private val trace = Evaluator(symbolTable, BasicOperations, sourceOps, EvaluatorOptions(recordDerivations = true))
        .trace(assertNotNull(symbolTable.getTemplate("main")))

    private fun processes(name: String): List<ProcessValue<BasicNumber>> =
        trace.getSystemValue().processes.filter { it.name == name }

    @Test
    fun cachedProcess_shouldHaveNoDerivation() {
        assertNull(trace.getDerivation(processes("cached_mine").single()))
    }

    @Test
    fun sameProcessWithDifferentArguments_shouldHaveOneDerivationEach() {
        val grades = processes("mill").map { assertNotNull(trace.getDerivation(it)).parameters.single().value }
        assertEquals(setOf(EStringLiteral<BasicNumber>("A"), EStringLiteral("B")), grades.toSet())
    }

    @Test
    fun emptyForEach_shouldRecordTheReadWithoutRows() {
        val derivation = assertNotNull(trace.getDerivation(processes("main").single()))
        assertEquals(0, derivation.forEach.single { it.rowRef == "row" }.read.records.size)
        assertTrue(derivation.origins.inputs.none { it.path.block == BlockPath(Section.INPUTS, listOf(4, 0)) })
        assertEquals(1, derivation.forEach.count { it.read.records.isEmpty() })
        assertEquals(processes("main").single().inputs.size, derivation.origins.inputs.size)
    }

    @Test
    fun sumInAnExchange_shouldBeAReadOfTheExchange() {
        val derivation = assertNotNull(trace.getDerivation(processes("main").single()))
        val (origin, reads) = derivation.exchangeReads.entries.single()
        assertEquals(BlockPath.of(Section.INPUTS, 0), origin.path.block)
        assertEquals(ReadKind.SUM, reads.single().kind)
    }

    @Test
    fun defaultRecord_shouldBeAReadOfTheVariable() {
        val derivation = assertNotNull(trace.getDerivation(processes("main").single()))
        val def = derivation.variables.single { (it.path as? SourcePath.Variable)?.name == "def" }
        assertEquals(ReadKind.DEFAULT_RECORD, def.reads.single().kind)
    }

    private fun variableNamesOf(name: String) = assertNotNull(trace.getDerivation(processes(name).single()))
        .variables.mapNotNull { (it.path as? SourcePath.Variable)?.name }

    @Test
    fun unusedVariables_shouldBeRecorded() {
        val names = variableNamesOf("main")
        assertTrue("unused" in names)
        assertTrue("unused_in_row" in names)
    }

    @Test
    fun unusedVariableWithAnError_shouldNotFailTheEvaluation() {
        assertFalse("broken_unused" in variableNamesOf("main"))
    }

    @Test
    fun parameters_shouldNotBeRecordedAsVariables() {
        val derivation = assertNotNull(trace.getDerivation(processes("main").single()))
        assertTrue(derivation.variables.none { it.path is SourcePath.Parameter })
        assertEquals(listOf("key"), derivation.parameters.map { it.path.name })
    }
}
