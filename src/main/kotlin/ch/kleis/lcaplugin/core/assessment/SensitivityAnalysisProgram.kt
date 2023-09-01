package ch.kleis.lcaplugin.core.assessment

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
