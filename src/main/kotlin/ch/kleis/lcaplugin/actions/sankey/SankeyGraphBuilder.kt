package ch.kleis.lcaplugin.actions.sankey

import ch.kleis.lcaplugin.core.assessment.ContributionAnalysis
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.graph.GraphLink
import ch.kleis.lcaplugin.core.graph.GraphNode
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.ExchangeValue
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
    fun buildContributionGraph(sankeyIndicator: MatrixColumnIndex<BasicNumber>): Graph {
        val portsWithObservedImpact = analysis.getObservablePorts().getElements().toSet()

        val completeGraph = portsWithObservedImpact.fold(
            Graph.empty().addNode(GraphNode(sankeyIndicator.getUID(), sankeyIndicator.getShortName()))
        ) { graph, port ->
            when (port) {
                is SubstanceValue<BasicNumber> -> {
                    graph.addNode(GraphNode(port.getUID(), port.getShortName()))
                        .addLinkIfNoCycle(
                            observableOrder,
                            port,
                            sankeyIndicator,
                            impactAmountForSubstance(sankeyIndicator, analysis, port)
                        )
                }

                is ProductValue<BasicNumber> -> {
                    val parentProcess = analysis.allocatedSystem.productToProcessMap[port]!!

                    val linksWithObservedImpact =
                        (parentProcess.inputs + parentProcess.biosphere).filter { parentProcessExchange ->
                            portsWithObservedImpact.contains(parentProcessExchange.port()) || parentProcessExchange.port() == sankeyIndicator
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
                            impactAmountForExchange(sankeyIndicator, analysis, port, exchange)
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

    private fun impactAmountForSubstance(
        observed: MatrixColumnIndex<BasicNumber>,
        inventory: ContributionAnalysis,
        substance: SubstanceValue<BasicNumber>,
    ): Double {
        val ops = BasicOperations
        val supply = absoluteScaleValue(ops, inventory.supply.quantityOf(substance)).value
        val characterizationFactor = absoluteScaleValue(ops, inventory.impactFactors.valueRatio(substance, observed)).value
        return supply * characterizationFactor
    }

    private fun impactAmountForExchange(
        observed: MatrixColumnIndex<BasicNumber>,
        inventory: ContributionAnalysis,
        product: ProductValue<BasicNumber>,
        exchange: ExchangeValue<BasicNumber>,
    ): Double {
        val ops = BasicOperations

        val supply = inventory.supply.quantityOf(product)
        val absoluteSupply = absoluteScaleValue(ops, supply).value
        val exchangeFactor = absoluteScaleValue(ops, exchange.quantity()).value  / supply.unit.scale
        val emissionFactor = when {
            (exchange.port() == observed) -> 1.0
            else -> {
                absoluteScaleValue(
                    ops,
                    inventory.impactFactors.valueRatio(exchange.port(), observed)
                ).value
            }
        }
        return absoluteSupply * exchangeFactor * emissionFactor
    }
}
