package ch.kleis.lcaac.core.assessment

import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.TechnoExchangeValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

/**
 * A row of the table of `lcaac trace`: how much of [output] is needed to supply [demandedProduct],
 * and the impacts embodied in that amount, per indicator or substance.
 */
data class TraceRow(
    val depth: Int,
    val demandedProduct: TechnoExchangeValue<BasicNumber>,
    val supply: QuantityValue<BasicNumber>,
    val output: MatrixColumnIndex<BasicNumber>,
    val impacts: Map<MatrixColumnIndex<BasicNumber>, QuantityValue<BasicNumber>>,
)

/**
 * The table of `lcaac trace` for an evaluated process: for each of its products, one row per product
 * of the supply chain, in the order of the trace.
 */
class TraceTableProgram(
    private val trace: EvaluationTrace<BasicNumber>,
) {
    fun run(): List<TraceRow> {
        val entryPoint = trace.getEntryPoint()
        val analysis = ContributionAnalysisProgram(trace.getSystemValue(), entryPoint).run()
        val observablePorts = analysis.getObservablePorts()
            .getElements()
            .sortedWith(trace.getComparator())
        val controllablePorts = analysis.getControllablePorts().getElements()
            .sortedBy { it.getShortName() }
        return entryPoint.products.flatMap { demandedProduct ->
            observablePorts.map {
                TraceRow(
                    trace.getDepthOf(it) ?: -1,
                    demandedProduct,
                    analysis.supplyOf(it),
                    it,
                    controllablePorts.associateWith { col ->
                        analysis.getPortContribution(it, col)
                    },
                )
            }
        }
    }
}
