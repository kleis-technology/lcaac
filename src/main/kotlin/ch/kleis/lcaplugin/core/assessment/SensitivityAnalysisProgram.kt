package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.core.math.dual.DualNumber
import ch.kleis.lcaplugin.core.math.dual.DualOperations
import ch.kleis.lcaplugin.core.matrix.ParameterVector

class SensitivityAnalysisProgram(
    private val system: SystemValue<DualNumber>,
    private val targetProcess: ProcessValue<DualNumber>,
    private val parameters: ParameterVector<DualNumber>,
) {
    fun run(): SensitivityAnalysis {
        if (parameters.names.size() == 0) {
            throw EvaluatorException("No quantitative parameters found")
        }
        if (system.processes.size >= 1000) {
            throw EvaluatorException("The current software version cannot perform the sensitivity analysis of a system with 1'000+ processes")
        }
        val results = AnalysisProgram(
            system,
            targetProcess,
            DualOperations(parameters.size()),
        ).run()
        return SensitivityAnalysis(
            targetProcess,
            results.impactFactors,
            results.supply,
            parameters,
        )
    }
}
