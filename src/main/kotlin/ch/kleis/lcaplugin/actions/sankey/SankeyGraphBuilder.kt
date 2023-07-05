package ch.kleis.lcaplugin.actions.sankey

import ch.kleis.lcaplugin.core.assessment.Inventory
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.graph.GraphLink
import ch.kleis.lcaplugin.core.graph.GraphNode
import ch.kleis.lcaplugin.core.lang.value.*

class SankeyGraphBuilder(
    private val allocatedSystem: SystemValue,
    private val inventory: Inventory,
) {
    // FIXME: detect cycles and
    // 1) warn ?
    // 2) remove ?
    // Add filters on (xor) 1) impact 2) relative impact 3) QQQ depending on user feebdack
    fun buildContributionGraph(sankeyIndicator: MatrixColumnIndex): Graph {
        val portsWithObservedImpact = inventory.getObservablePorts().getElements().toSet()

        return portsWithObservedImpact.fold(
            Graph.empty().addNode(GraphNode(sankeyIndicator.getUID(), sankeyIndicator.getDisplayName()))
        ) { graph, port ->
            when (port) {
                is SubstanceValue -> {
                    graph.addNode(GraphNode(port.getUID(), port.getDisplayName()))
                        .addLink(
                            GraphLink(
                                port.getUID(),
                                sankeyIndicator.getUID(),
                                impactAmountForSubstance(sankeyIndicator, inventory, port)
                            )
                        )
                }

                is ProductValue -> {
                    val parentProcess = allocatedSystem.productToProcessMap[port]!!

                    val linksWithObservedImpact = (parentProcess.inputs + parentProcess.biosphere).filter { parentProcessExchange ->
                        portsWithObservedImpact.contains(parentProcessExchange.port()) || parentProcessExchange.port() == sankeyIndicator
                    }

                    linksWithObservedImpact.fold(graph.addNode(GraphNode(port.getUID(), port.getDisplayName()))) { accumulatorGraph, exchange ->
                        accumulatorGraph.addLink(
                            GraphLink(
                                port.getUID(),
                                exchange.port().getUID(),
                                impactAmountForExchange(sankeyIndicator, inventory, port, exchange)))
                    }
                }

                else -> graph
            }
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