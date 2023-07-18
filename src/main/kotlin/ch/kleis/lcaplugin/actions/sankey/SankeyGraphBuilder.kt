package ch.kleis.lcaplugin.actions.sankey

import ch.kleis.lcaplugin.core.assessment.Inventory
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.graph.GraphLink
import ch.kleis.lcaplugin.core.graph.GraphNode
import ch.kleis.lcaplugin.core.lang.value.*

class SankeyGraphBuilder(
        private val allocatedSystem: SystemValue,
        private val inventory: Inventory,
        private val comparator: Comparator<MatrixColumnIndex>,
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
                                    sankeyIndicator,
                                    port,
                                    sankeyIndicator,
                                    impactAmountForSubstance(sankeyIndicator, inventory, port)
                            )
                }

                is ProductValue -> {
                    val parentProcess = allocatedSystem.productToProcessMap[port]!!

                    val linksWithObservedImpact = (parentProcess.inputs + parentProcess.biosphere).filter { parentProcessExchange ->
                        portsWithObservedImpact.contains(parentProcessExchange.port()) || parentProcessExchange.port() == sankeyIndicator
                    }

                    linksWithObservedImpact.fold(graph.addNode(GraphNode(port.getUID(), port.getShortName()))) { accumulatorGraph, exchange ->
                        accumulatorGraph.addLinkIfNoCycle(
                                sankeyIndicator,
                                port,
                                exchange.port(),
                                impactAmountForExchange(sankeyIndicator, inventory, port, exchange))
                    }
                }

                else -> graph
            }
        }

        return completeGraph
    }

    private fun Graph.addLinkIfNoCycle(
            sankeyIndicator: MatrixColumnIndex,
            source: MatrixColumnIndex,
            target: MatrixColumnIndex,
            value: Double
    ): Graph {
        val comparisonResult = when {
            target == sankeyIndicator -> -1
            else -> comparator.compare(source, target)
        }
        return if (comparisonResult < 0) {
            this.addLink(GraphLink(source.getUID(), target.getUID(), value))
        } else {
            val cycleUID = "cycle back to ${target.getShortName()}"
            this.addNode(GraphNode(cycleUID, cycleUID))
                    .addLink(GraphLink(
                            source.getUID(),
                            cycleUID,
                            value)
                    )
        }
    }

    private fun impactAmountForSubstance(observed: MatrixColumnIndex, inventory: Inventory, substance: SubstanceValue): Double {
        return inventory.impactFactors.valueRatio(substance, observed).amount *
                inventory.supply.quantityOf(substance).amount
    }

    private fun impactAmountForExchange(
            observed: MatrixColumnIndex,
            inventory: Inventory,
            product: ProductValue,
            exchange: ExchangeValue
    ): Double {
        val valueRatioForObservedImpact = when {
            (exchange.port() == observed) -> 1.0
            else -> inventory.impactFactors.valueRatio(exchange.port(), observed).amount
        }

        return valueRatioForObservedImpact *
                inventory.supply.quantityOf(product).amount *
                exchange.quantity().amount
    }
}
