package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations

class ContributionAnalysisProgram(
    private val system: SystemValue<BasicNumber>,
    private val targetProcess: ProcessValue<BasicNumber>,
) {
    fun run(): ContributionAnalysis {
        val results = AnalysisProgram(system, targetProcess, BasicOperations).run()
        return ContributionAnalysis(
            results.impactFactors,
            results.supply,
            results.allocatedSystem,
        )
    }
}
