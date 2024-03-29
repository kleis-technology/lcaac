package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.EProductSpec
import ch.kleis.lcaac.core.lang.expression.FromProcess
import ch.kleis.lcaac.core.lang.expression.MatchLabels
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EvaluatorTest {
    private val ops = BasicOperations
    private val sourceOps = mockk<DataSourceOperations<BasicNumber>>()

    @Test
    fun arena_shouldHandleDefaultRecordOf() {
        // given
        val file = LcaLangFixture.parser("""
            datasource source {
                location = "file.csv"
                schema {
                    mass = 1 kg
                }
            }
            
            process p {
                params {
                    row = default_record from source
                }
                products {
                    1 kg carrot
                }
                impacts {
                    row.mass co2
                }
            }
        """.trimIndent()).lcaFile()
        val loader = Loader(ops)
        val symbolTable = loader.load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
        val spec = EProductSpec<BasicNumber>(
            name = "carrot",
            fromProcess = FromProcess("p", MatchLabels(emptyMap())),
        )
        val evaluator = Evaluator(symbolTable, ops, sourceOps)

        // when
        val trace = evaluator.trace(setOf(spec))
        val program = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint())
        val analysis = program.run()

        // then
        val port = analysis.getObservablePorts().get("carrot from p{}{row={mass=1.0 kg}}")
        val indicator = analysis.getControllablePorts().get("co2")
        val expected = QuantityValue(BasicNumber(1.0), UnitValue(UnitSymbol.of("kg"), 1.0, Dimension.of("mass")))
        val actual = analysis.getPortContribution(port, indicator)
        assertEquals(expected, actual)
    }
    
    @Test
    fun arena_shouldHandleKnowledgeCorrectly() {
        // given
        val file = LcaLangFixture.parser(
            """
                process a_proc {
                    products {
                        1 kg a
                    }
                    inputs {
                        1 kg b from b_proc
                        1 l c from c_proc
                    }
                }
                
                process b_proc {
                    products {
                        1 kg b
                    }
                    impacts {
                        1 kg gwp
                    }
                }
                
                process c_proc {
                    products {
                        1 l c
                    }
                    inputs {
                        1kg b from b_proc
                    }
                    impacts {
                        1 kg gwp
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(ops)
        val symbolTable = loader.load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
        val spec = EProductSpec<BasicNumber>(
            name = "a",
            fromProcess = FromProcess("a_proc", MatchLabels(emptyMap())),
        )
        val evaluator = Evaluator(symbolTable, ops, sourceOps)

        // when
        val trace = evaluator.trace(setOf(spec))
        val program = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint())
        val analysis = program.run()

        // then
        val port = analysis.getObservablePorts().get("a from a_proc{}{}")
        val indicator = analysis.getControllablePorts().get("gwp")
        val expected = QuantityValue(BasicNumber(3.0), UnitValue(UnitSymbol.of("kg"), 1.0, Dimension.of("mass")))
        val actual = analysis.getPortContribution(port, indicator)
        assertEquals(expected, actual)
    }


    @Test
    fun arena_with1HopLoop() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p1 {
                    products {
                        1 kg A
                    }
                    inputs {
                        1 kg B
                    }
                }
                process p2 {
                    products {
                        1 kg B
                    }
                    inputs {
                        0.5 kg A
                    }
                    emissions {
                        1 kg C
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(ops)
        val symbolTable = loader.load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
        val spec = EProductSpec<BasicNumber>(
            name = "A",
            fromProcess = FromProcess("p1", MatchLabels(emptyMap())),
        )
        val evaluator = Evaluator(symbolTable, ops, sourceOps)

        // when
        val trace = evaluator.trace(setOf(spec))
        val program = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint())
        val analysis = program.run()

        // then
        val port = analysis.getObservablePorts().get("A from p1{}{}")
        val indicator = analysis.getControllablePorts().get("C")
        val expected = QuantityValue(BasicNumber(4.0), UnitValue(UnitSymbol.of("kg"), 1.0, Dimension.of("mass")))
        val actual = analysis.getPortContribution(port, indicator)
        assertEquals(expected, actual)
    }

    @Test
    fun arena_withSelfLoop() {
        val file = LcaLangFixture.parser(
            """
                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        0.5 kg carrot
                    }
                    impacts {
                        1 kg GWP
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(ops)
        val symbolTable = loader.load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
        val spec = EProductSpec<BasicNumber>(
            name = "carrot",
            fromProcess = FromProcess("p", MatchLabels(emptyMap())),
        )
        val evaluator = Evaluator(symbolTable, ops, sourceOps)

        // when
        val trace = evaluator.trace(setOf(spec))
        val program = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint())
        val analysis = program.run()

        // then
        val port = analysis.getObservablePorts().get("carrot from p{}{}")
        val indicator = analysis.getControllablePorts().get("GWP")
        val expected = QuantityValue(BasicNumber(4.0), UnitValue(UnitSymbol.of("kg"), 1.0, Dimension.of("mass")))
        val actual = analysis.getPortContribution(port, indicator)
        assertEquals(expected, actual)
    }

    @Test
    fun arena_withMoreIntermediates() {
        val file = LcaLangFixture.parser(
            """
                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 kg wheat
                    }
                }
                
                process q {
                    products {
                        1 kg wheat
                    }
                    inputs {
                        1 kWh electricity
                    }
                }
                
                process r {
                    products {
                        1 kWh electricity
                    }
                    impacts {
                        1 kg GWP
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(ops)
        val symbolTable = loader.load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
        val spec = EProductSpec<BasicNumber>(
            name = "carrot",
            fromProcess = FromProcess("p", MatchLabels(emptyMap())),
        )
        val evaluator = Evaluator(symbolTable, ops, sourceOps)

        // when
        val trace = evaluator.trace(setOf(spec))
        val program = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint())
        val analysis = program.run()

        // then
        val port = analysis.getObservablePorts().get("carrot from p{}{}")
        val indicator = analysis.getControllablePorts().get("GWP")
        val expected = QuantityValue(BasicNumber(1.0), UnitValue(UnitSymbol.of("kg"), 1.0, Dimension.of("mass")))
        val actual = analysis.getPortContribution(port, indicator)
        assertEquals(expected, actual)
    }

    @Test
    fun arena_singleIntermediary() {
        val file = LcaLangFixture.parser(
            """
                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 kg wheat
                    }
                }
                
                process q {
                    products {
                        1 kg wheat
                    }
                    impacts {
                        1 kg GWP
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(ops)
        val symbolTable = loader.load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
        val spec = EProductSpec<BasicNumber>(
            name = "carrot",
            fromProcess = FromProcess("p", MatchLabels(emptyMap())),
        )
        val evaluator = Evaluator(symbolTable, ops, sourceOps)

        // when
        val trace = evaluator.trace(setOf(spec))
        val program = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint())
        val analysis = program.run()

        // then
        val port = analysis.getObservablePorts().get("carrot from p{}{}")
        val indicator = analysis.getControllablePorts().get("GWP")
        val expected = QuantityValue(BasicNumber(1.0), UnitValue(UnitSymbol.of("kg"), 1.0, Dimension.of("mass")))
        val actual = analysis.getPortContribution(port, indicator)
        assertEquals(expected, actual)
    }

    @Test
    fun arena_withSubstance() {
        val file = LcaLangFixture.parser(
            """
                process p {
                    products {
                        1 kg carrot
                    }
                    emissions {
                        1 kg wheat(compartment="agriculture")
                    }
                }
                
                substance wheat {
                    name = "wheat"
                    type = Emission
                    compartment = "agriculture"
                    reference_unit = kg
                    impacts {
                        1 kg GWP
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(ops)
        val symbolTable = loader.load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
        val spec = EProductSpec<BasicNumber>(
            name = "carrot",
            fromProcess = FromProcess("p", MatchLabels(emptyMap())),
        )
        val evaluator = Evaluator(symbolTable, ops, sourceOps)

        // when
        val trace = evaluator.trace(setOf(spec))
        val program = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint())
        val analysis = program.run()

        // then
        val port = analysis.getObservablePorts().get("carrot from p{}{}")
        val indicator = analysis.getControllablePorts().get("GWP")
        val expected = QuantityValue(BasicNumber(1.0), UnitValue(UnitSymbol.of("kg"), 1.0, Dimension.of("mass")))
        val actual = analysis.getPortContribution(port, indicator)
        assertEquals(expected, actual)
    }

    @Test
    fun arena_withIntermediaryAndSubstance() {
        val file = LcaLangFixture.parser(
            """
                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 kg wheat
                    }
                }
                
                process q {
                    products {
                        1 kg wheat
                    }
                    emissions {
                        1 kWh electricity(compartment="none")
                    }
                }
                
                
                substance electricity {
                    name = "electricity"
                    type = Emission
                    compartment = "none"
                    reference_unit = kWh
                    impacts {
                        1 kg GWP
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(ops)
        val symbolTable = loader.load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
        val spec = EProductSpec<BasicNumber>(
            name = "carrot",
            fromProcess = FromProcess("p", MatchLabels(emptyMap())),
        )
        val evaluator = Evaluator(symbolTable, ops, sourceOps)

        // when
        val trace = evaluator.trace(setOf(spec))
        val program = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint())
        val analysis = program.run()

        // then
        val port = analysis.getObservablePorts().get("carrot from p{}{}")
        val indicator = analysis.getControllablePorts().get("GWP")
        val expected = QuantityValue(BasicNumber(1.0), UnitValue(UnitSymbol.of("kg"), 1.0, Dimension.of("mass")))
        val actual = analysis.getPortContribution(port, indicator)
        assertEquals(expected, actual)
    }

}
