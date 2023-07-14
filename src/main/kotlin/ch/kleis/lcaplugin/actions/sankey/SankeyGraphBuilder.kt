package ch.kleis.lcaplugin.actions.sankey

import ch.kleis.lcaplugin.core.assessment.Inventory
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.graph.GraphLink
import ch.kleis.lcaplugin.core.graph.GraphNode
import ch.kleis.lcaplugin.core.lang.value.*
import kotlin.math.abs

class SankeyGraphBuilder(
    private val allocatedSystem: SystemValue,
    private val inventory: Inventory,
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

                    linksWithObservedImpact.fold(graph.addNode(GraphNode(port.getUID(), port.getShortName()))) { accumulatorGraph, exchange ->
                        accumulatorGraph.addLinkIfNoCycle(
                            GraphLink(
                                port.getUID(),
                                exchange.port().getUID(),
                                impactAmountForExchange(sankeyIndicator, inventory, port, exchange)))
                    }
                }

                else -> graph
            }
        }

        return completeGraph
    }

    private fun Graph.addLinkIfNoCycle(link: GraphLink): Graph =
        if (this.links.any { it.source == link.target && it.target == link.source }) {
            this
        } else {
            this.addLink(link)
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

        return abs(valueRatioForObservedImpact *
            inventory.supply.quantityOf(product).amount *
            exchange.quantity().amount)
    }
}