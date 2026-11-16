package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorOptions
import ch.kleis.lcaac.core.lang.evaluator.derivation.ProcessDerivation
import ch.kleis.lcaac.core.lang.evaluator.derivation.ReadKind
import ch.kleis.lcaac.core.lang.evaluator.derivation.RowRef
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.source.BlockPath
import ch.kleis.lcaac.core.lang.source.Section
import ch.kleis.lcaac.core.lang.source.SourcePath
import ch.kleis.lcaac.core.lang.value.ProcessValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.every
import io.mockk.mockk
import kotlin.test.*

class DerivationRecordingTest {
    private val ops = BasicOperations

    private val content = """
        variables {
            grid_loss = 10 percent
        }

        datasource machines {
            location = "machines.csv"
            schema {
                id = "m1"
                consumption = 1 kWh
            }
        }

        datasource parts {
            location = "parts.csv"
            schema {
                id = "p1"
                mass = 1 kg
            }
        }

        process assembly {
            params {
                machine = "m1"
            }
            variables {
                mach = lookup machines match id = machine
                energy = mach.consumption * (1 u + grid_loss)
                steel_mass = sum(parts, mass)
            }
            products {
                1 p widget
            }
            inputs {
                energy electricity
                steel_mass steel from steel_mill(grade = "A")
                for_each part from parts {
                    variables {
                        wrap = 10 percent * part.mass
                    }
                    wrap packaging
                }
            }
        }

        process steel_mill {
            params {
                grade = "B"
            }
            products {
                1 kg steel
            }
        }
    """.trimIndent()

    private fun quantity(amount: Double, symbol: String, scale: Double, dimension: String) =
        EQuantityScale(BasicNumber(amount), EUnitLiteral(UnitSymbol.of(symbol), scale, Dimension.of(dimension)))

    private val sourceOps = mockk<DataSourceOperations<BasicNumber>>().also {
        every { it.getFirst(any()) } returns ERecord(mapOf(
            "id" to EStringLiteral("m1"),
            "consumption" to quantity(2.0, "kWh", 3.6e6, "energy"),
        ))
        every { it.getAll(any()) } returns sequenceOf(
            ERecord(mapOf("id" to EStringLiteral("p1"), "mass" to quantity(1.5, "kg", 1.0, "mass"))),
            ERecord(mapOf("id" to EStringLiteral("p2"), "mass" to quantity(0.5, "kg", 1.0, "mass"))),
        )
        every { it.sumProduct(any(), listOf("mass")) } returns quantity(2.0, "kg", 1.0, "mass")
    }

    private val symbolTable = Loader(ops).load(sequenceOf(LcaLangFixture.parser(content).lcaFile()), listOf(LoaderOption.WITH_PRELUDE))
    private val assemblyTemplate = assertNotNull(symbolTable.getTemplate("assembly"))
    private val assembly = SourcePath.Process(ProcessKey("assembly"))
    private val forEach = SourcePath.Exchange(assembly, BlockPath.of(Section.INPUTS, 2))

    private fun trace(options: EvaluatorOptions = EvaluatorOptions(recordDerivations = true)): EvaluationTrace<BasicNumber> =
        Evaluator(symbolTable, ops, sourceOps, options).trace(assemblyTemplate)

    private fun EvaluationTrace<BasicNumber>.process(name: String): ProcessValue<BasicNumber> =
        getSystemValue().processes.first { it.name == name }

    private fun EvaluationTrace<BasicNumber>.derivation(name: String): ProcessDerivation<BasicNumber> =
        assertNotNull(getDerivation(process(name)), "no derivation for $name")

    private fun scaleOf(expression: DataExpression<BasicNumber>?): Double =
        assertIs<EQuantityScale<BasicNumber>>(expression).scale.value

    @Test
    fun withoutOption_shouldNotRecord() {
        val trace = trace(EvaluatorOptions())
        assertNull(trace.getDerivation(trace.getEntryPoint()))
    }

    @Test
    fun parameters_shouldGiveArgumentAndDefault() {
        val trace = trace()

        val machine = trace.derivation("assembly").parameters.single()
        assertEquals(SourcePath.Parameter(assembly.process, "machine"), machine.path)
        assertEquals(EStringLiteral("m1"), machine.default)
        assertEquals(EStringLiteral("m1"), machine.value)

        val grade = trace.derivation("steel_mill").parameters.single()
        assertEquals(EStringLiteral("B"), grade.default)
        assertEquals(EStringLiteral("A"), grade.value)
    }

    @Test
    fun variables_shouldGiveExpressionValueAndReads() {
        val derivation = trace().derivation("assembly")
        val variables = derivation.variables.associateBy { it.path }

        val energy = assertNotNull(variables[SourcePath.Variable(assembly, "energy")])
        assertEquals(assemblyTemplate.locals["energy"], energy.expression)
        assertEquals(2.2, scaleOf(energy.value), 1e-9)

        val mach = assertNotNull(variables[SourcePath.Variable(assembly, "mach")])
        val lookup = mach.reads.single()
        assertEquals(ReadKind.LOOKUP, lookup.kind)
        assertEquals("machines", lookup.dataSource.config.name)
        assertEquals(EStringLiteral("m1"), lookup.records.single().entries["id"])

        val steelMass = assertNotNull(variables[SourcePath.Variable(assembly, "steel_mass")])
        val sum = steelMass.reads.single()
        assertEquals(ReadKind.SUM, sum.kind)
        assertEquals(listOf("mass"), sum.columns)
        assertEquals(2, sum.records.size)
        assertEquals(2.0, scaleOf(steelMass.value), 1e-9)

        val gridLoss = assertNotNull(variables[SourcePath.Global("grid_loss")])
        assertEquals(10.0, scaleOf(gridLoss.value), 1e-9)
    }

    @Test
    fun units_shouldNotBeRecorded() {
        val paths = trace().derivation("assembly").variables.map { it.path }
        assertFalse(SourcePath.Global("kWh") in paths)
        assertFalse(SourcePath.Global("percent") in paths)
    }

    @Test
    fun forEach_shouldGiveRowsAndOneValuePerRow() {
        val derivation = trace().derivation("assembly")

        val read = derivation.forEach.single()
        assertEquals(forEach, read.path)
        assertEquals("part", read.rowRef)
        assertEquals(2, read.read.records.size)

        val wraps = derivation.variables.filter { it.path == SourcePath.Variable(assembly, "wrap", forEach.block) }
        assertEquals(listOf(listOf(RowRef(forEach, 0)), listOf(RowRef(forEach, 1))), wraps.map { it.rows })
        assertEquals(listOf(15.0, 5.0), wraps.map { scaleOf(it.value) })
    }

    @Test
    fun origins_shouldMatchTheExchangesOfTheProcessValue() {
        val trace = trace()
        val process = trace.process("assembly")
        val origins = trace.derivation("assembly").origins

        assertEquals(
            listOf(
                BlockPath.of(Section.INPUTS, 0),
                BlockPath.of(Section.INPUTS, 1),
                BlockPath(Section.INPUTS, listOf(2, 0)),
                BlockPath(Section.INPUTS, listOf(2, 0)),
            ),
            origins.inputs.map { it.path.block },
        )
        assertEquals(listOf(emptyList(), emptyList(), listOf(RowRef(forEach, 0)), listOf(RowRef(forEach, 1))), origins.inputs.map { it.rows })
        assertEquals(1, origins.products.size)
    }

    @Test
    fun expressionOf_shouldGiveTheExchangeAsWritten() {
        val trace = trace()
        trace.getSystemValue().processes.forEach { process ->
            val derivation = trace.derivation(process.name)
            assertEquals(process.products.size, derivation.origins.products.size)
            assertEquals(process.inputs.size, derivation.origins.inputs.size)
            process.inputs.zip(derivation.origins.inputs).forEach { (exchange, origin) ->
                val written = assertIs<ETechnoExchange<BasicNumber>>(derivation.expressionOf(origin))
                assertEquals(exchange.product.name, written.product.name)
            }
        }
        // Written as in the source, before parameters are replaced
        val steel = trace.derivation("assembly").let { it.expressionOf(it.origins.inputs[1]) }
        assertEquals(EDataRef("steel_mass"), steel?.quantity)
    }
}
