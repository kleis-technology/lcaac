package ch.kleis.lcaac.core.assessment

import ch.kleis.lcaac.core.allocation.Allocation
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.TechnoExchangeValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations

/**
 * A row of the table of `lcaac trace`: how much of [output] is needed to supply [demandedProduct],
 * and the impacts embodied in that amount, per indicator or substance. [allocation] is the share of
 * [demandedProduct] among the products of the evaluated process, the factor applied to [supply] and
 * [impacts] for it, as `lcaac trace` shows it.
 */
data class TraceRow(
    val depth: Int,
    val demandedProduct: TechnoExchangeValue<BasicNumber>,
    val supply: QuantityValue<BasicNumber>,
    val output: MatrixColumnIndex<BasicNumber>,
    val impacts: Map<MatrixColumnIndex<BasicNumber>, QuantityValue<BasicNumber>>,
    val allocation: Double,
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
        val allocation = Allocation(BasicOperations)
        return entryPoint.products.flatMap { demandedProduct ->
            val share = allocation.ratioOf(demandedProduct, entryPoint)
            observablePorts.map {
                TraceRow(
                    trace.getDepthOf(it) ?: -1,
                    demandedProduct,
                    analysis.supplyOf(it),
                    it,
                    controllablePorts.associateWith { col ->
                        analysis.getPortContribution(it, col)
                    },
                    share,
                )
            }
        }
    }
}
