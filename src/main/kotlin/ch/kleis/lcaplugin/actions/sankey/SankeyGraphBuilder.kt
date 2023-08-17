package ch.kleis.lcaplugin.actions.sankey

import ch.kleis.lcaplugin.core.assessment.Inventory
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.graph.GraphLink
import ch.kleis.lcaplugin.core.graph.GraphNode
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.*

class SankeyGraphBuilder(
    private val allocatedSystem: SystemValue,
    private val inventory: Inventory,
    private val observableOrder: Comparator<MatrixColumnIndex>,
) {
    fun buildContributionGraph(sankeyIndicator: MatrixColumnIndex): Graph {
        val portsWithObservedImpact = inventory.getObservablePorts().getElements().toSet()

        val completeGraph = portsWithObservedImpact.fold(
            Graph.empty().addNode(GraphNode(sankeyIndicator.getUID(), sankeyIndicator.getShortName()))
        ) { graph, port ->
            when (port) {
                is SubstanceValue -> {
                    graph.addNode(GraphNode(port.getUID(), port.getShortName()))
                        .addLinkIfNoCycle(
                            observableOrder,
                            port,
                            sankeyIndicator,
                            impactAmountForSubstance(sankeyIndicator, inventory, port)
                        )
                }

                is ProductValue -> {
                    val parentProcess = allocatedSystem.productToProcessMap[port]!!

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
                            impactAmountForExchange(sankeyIndicator, inventory, port, exchange)
                        )
                    }
                }

                else -> graph
            }
        }

        return completeGraph
    }

    private fun Graph.addLinkIfNoCycle(
        observableOrder: Comparator<MatrixColumnIndex>,
        source: MatrixColumnIndex,
        target: MatrixColumnIndex,
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
        observed: MatrixColumnIndex,
        inventory: Inventory,
        substance: SubstanceValue
    ): Double {
        val a = inventory.impactFactors.valueRatio(substance, observed).referenceValue()
        val b = inventory.supply.quantityOf(substance).referenceValue()
        return a * b
    }

    private fun impactAmountForExchange(
        observed: MatrixColumnIndex,
        inventory: Inventory,
        product: ProductValue,
        exchange: ExchangeValue
    ): Double {
        val valueRatioForObservedImpact = when {
            (exchange.port() == observed) -> 1.0
            else -> inventory.impactFactors.valueRatio(exchange.port(), observed).referenceValue()
        }

        return valueRatioForObservedImpact *
                inventory.supply.quantityOf(product).referenceValue() *
                exchange.quantity().referenceValue()
    }
}
