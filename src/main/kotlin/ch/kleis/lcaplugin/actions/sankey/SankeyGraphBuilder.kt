package ch.kleis.lcaplugin.actions.sankey

import ch.kleis.lcaplugin.core.assessment.ContributionAnalysis
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.graph.GraphLink
import ch.kleis.lcaplugin.core.graph.GraphNode
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.ProductValue
import ch.kleis.lcaplugin.core.lang.value.SubstanceValue
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import ch.kleis.lcaplugin.core.matrix.absoluteScaleValue

class SankeyGraphBuilder(
    private val analysis: ContributionAnalysis,
    private val observableOrder: Comparator<MatrixColumnIndex<BasicNumber>>,
) {
    fun buildContributionGraph(indicator: MatrixColumnIndex<BasicNumber>): Graph {
        val portsWithObservedImpact = analysis.getObservablePorts().getElements().toSet()

        val completeGraph = portsWithObservedImpact.fold(
            Graph.empty().addNode(GraphNode(indicator.getUID(), indicator.getShortName()))
        ) { graph, port ->
            when (port) {
                is SubstanceValue<BasicNumber> -> {
                    graph.addNode(GraphNode(port.getUID(), port.getShortName()))
                        .addLinkIfNoCycle(
                            observableOrder,
                            port,
                            indicator,
                            absoluteScaleValue(BasicOperations, analysis.getPortContribution(port, indicator)).value,
                        )
                }

                is ProductValue<BasicNumber> -> {
                    val process = analysis.findOwnerOf(port) ?: throw IllegalStateException()

                    val linksWithObservedImpact =
                        (process.inputs + process.biosphere).filter { exchange ->
                            portsWithObservedImpact.contains(exchange.port()) || exchange.port() == indicator
                        }

                    linksWithObservedImpact.fold(
                        graph.addNode(
                            GraphNode(
                                port.getUID(),
                                port.getShortName()
                            )
                        )
                    ) { accumulatorGraph, exchange ->
                        accumulatorGraph.addLinkIfNoCycle(
                            observableOrder,
                            port,
                            exchange.port(),
                            absoluteScaleValue(
                                BasicOperations,
                                analysis.getExchangeContribution(port, exchange, indicator)
                            ).value,
                        )
                    }
                }

                else -> graph
            }
        }

        return completeGraph
    }

    private fun Graph.addLinkIfNoCycle(
        observableOrder: Comparator<MatrixColumnIndex<BasicNumber>>,
        source: MatrixColumnIndex<BasicNumber>,
        target: MatrixColumnIndex<BasicNumber>,
        value: Double,
    ): Graph {
        // The observable wrt which we are computing is not in the matrix: it will raise a not found exception.
        // It is always the target, and always "deeper" in the graph than everything else.
        val compareResult = try {
            observableOrder.compare(source, target)
        } catch (e: EvaluatorException) {
            -1
        }

        return if (source == target || 0 < compareResult) {
            this
        } else {
            this.addLink(GraphLink(source.getUID(), target.getUID(), value))
        }
    }
}
