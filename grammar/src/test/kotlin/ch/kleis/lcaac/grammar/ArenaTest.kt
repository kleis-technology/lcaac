package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.protocol.Arena
import ch.kleis.lcaac.core.lang.expression.EProductSpec
import ch.kleis.lcaac.core.lang.expression.FromProcess
import ch.kleis.lcaac.core.lang.expression.MatchLabels
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.LcaLangFixture.Companion.lcaFile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class ArenaTest {
    @Test
    fun arena_withSelfLoop() {
        val file = lcaFile(
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
        )
        val loader = Loader(BasicOperations)
        val symbolTable = loader.load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
        val spec = EProductSpec<BasicNumber>(
            name = "carrot",
            fromProcess = FromProcess("p", MatchLabels(emptyMap())),
        )
        val arena = Arena(symbolTable, setOf(spec), BasicOperations)

        // when
        val trace = arena.run()
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
        val file = lcaFile(
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
        )
        val loader = Loader(BasicOperations)
        val symbolTable = loader.load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
        val spec = EProductSpec<BasicNumber>(
            name = "carrot",
            fromProcess = FromProcess("p", MatchLabels(emptyMap())),
        )
        val arena = Arena(symbolTable, setOf(spec), BasicOperations)

        // when
        val trace = arena.run()
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
        val file = lcaFile(
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
        )
        val loader = Loader(BasicOperations)
        val symbolTable = loader.load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
        val spec = EProductSpec<BasicNumber>(
            name = "carrot",
            fromProcess = FromProcess("p", MatchLabels(emptyMap())),
        )
        val arena = Arena(symbolTable, setOf(spec), BasicOperations)

        // when
        val trace = arena.run()
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
        val file = lcaFile(
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
        )
        val loader = Loader(BasicOperations)
        val symbolTable = loader.load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
        val spec = EProductSpec<BasicNumber>(
            name = "carrot",
            fromProcess = FromProcess("p", MatchLabels(emptyMap())),
        )
        val arena = Arena(symbolTable, setOf(spec), BasicOperations)

        // when
        val trace = arena.run()
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
        val file = lcaFile(
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
        )
        val loader = Loader(BasicOperations)
        val symbolTable = loader.load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
        val spec = EProductSpec<BasicNumber>(
            name = "carrot",
            fromProcess = FromProcess("p", MatchLabels(emptyMap())),
        )
        val arena = Arena(symbolTable, setOf(spec), BasicOperations)

        // when
        val trace = arena.run()
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
