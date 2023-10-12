package ch.kleis.lcaac.core.assessment

import ch.kleis.lcaac.core.lang.value.ProcessValue
import ch.kleis.lcaac.core.lang.value.SystemValue
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations

class ContributionAnalysisProgram(
    private val system: SystemValue<BasicNumber>,
    private val targetProcess: ProcessValue<BasicNumber>,
) {
    fun run(): ContributionAnalysis<BasicNumber, BasicMatrix> {
        val results = AnalysisProgram(system, targetProcess, BasicOperations).run()
        return ContributionAnalysis(
            targetProcess,
            results.impactFactors,
            results.intensity,
            results.allocatedSystem,
            BasicOperations,
        )
    }
}
