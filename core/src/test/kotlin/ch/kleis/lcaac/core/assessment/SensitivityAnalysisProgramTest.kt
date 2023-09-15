package ch.kleis.lcaac.core.assessment

import ch.kleis.lcaac.core.ParameterName
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.value.ProcessValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.SystemValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.dual.DualNumber
import ch.kleis.lcaac.core.math.dual.DualOperations
import ch.kleis.lcaac.core.matrix.IndexedCollection
import ch.kleis.lcaac.core.matrix.ParameterVector
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SensitivityAnalysisProgramTest {

    @Test
    fun run_when1000Processes_shouldThrow() {
        // given
        val ops = DualOperations(1)
        val system = SystemValue(
            processes = IntRange(1, 1000).map {
                ProcessValue<DualNumber>(
                    name = "$it"
                )
            }.toSet()
        )
        val targetProcess = system.processes.first()
        val parameters = ParameterVector(
            names = IndexedCollection(listOf(ParameterName("p"))),
            data = listOf(
                QuantityValue(
                    ops.pure(1.0),
                    UnitValue(UnitSymbol.of("kg"), 1.0, Dimension.Companion.of("mass")),
                )
            )
        )
        val program = SensitivityAnalysisProgram(system, targetProcess, parameters)

        // when/then
        val e = assertThrows(EvaluatorException::class.java) {
            program.run()
        }
        assertEquals(
            "The current software version cannot perform the sensitivity analysis of a system with 1'000+ processes",
            e.message
        )
    }

    @Test
    fun run_whenNoParameters_shouldThrow() {
        // given
        val system = SystemValue<DualNumber>(
            processes = setOf(ProcessValue("p"))
        )
        val targetProcess = system.processes.first()
        val parameters = ParameterVector<DualNumber>(
            names = IndexedCollection(emptyList()),
            data = emptyList(),
        )
        val program = SensitivityAnalysisProgram(system, targetProcess, parameters)

        // when/then
        val e = assertThrows(EvaluatorException::class.java) {
            program.run()
        }
        assertEquals(
            "No quantitative parameter found",
            e.message
        )
    }
}
