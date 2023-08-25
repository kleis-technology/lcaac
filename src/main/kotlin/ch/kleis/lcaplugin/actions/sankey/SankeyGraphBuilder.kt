package ch.kleis.lcaplugin.actions.sankey

import ch.kleis.lcaplugin.core.assessment.Inventory
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.graph.GraphLink
import ch.kleis.lcaplugin.core.graph.GraphNode
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.core.math.basic.BasicMatrix
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import ch.kleis.lcaplugin.core.matrix.absoluteScaleValue

class SankeyGraphBuilder(
    private val allocatedSystem: SystemValue<BasicNumber>,
    private val inventory: Inventory<BasicNumber, BasicMatrix>,
    private val observableOrder: Comparator<MatrixColumnIndex<BasicNumber>>,
) {
    fun buildContributionGraph(sankeyIndicator: MatrixColumnIndex<BasicNumber>): Graph {
        val portsWithObservedImpact = inventory.getObservablePorts().getElements().toSet()

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
                            impactAmountForSubstance(sankeyIndicator, inventory, port)
                        )
                }

                is ProductValue<BasicNumber> -> {
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
        inventory: Inventory<BasicNumber, BasicMatrix>,
        substance: SubstanceValue<BasicNumber>,
    ): Double {
        val ops = BasicOperations.INSTANCE
        val a = absoluteScaleValue(ops, inventory.impactFactors.valueRatio(substance, observed)).value
        val b = absoluteScaleValue(ops, inventory.supply.quantityOf(substance)).value
        return a * b
    }

    private fun impactAmountForExchange(
        observed: MatrixColumnIndex<BasicNumber>,
        inventory: Inventory<BasicNumber, BasicMatrix>,
        product: ProductValue<BasicNumber>,
        exchange: ExchangeValue<BasicNumber>,
    ): Double {
        val ops = BasicOperations.INSTANCE
        val valueRatioForObservedImpact = when {
            (exchange.port() == observed) -> 1.0
            else -> absoluteScaleValue(
                BasicOperations.INSTANCE,
                inventory.impactFactors.valueRatio(exchange.port(), observed)
            ).value
        }
        return valueRatioForObservedImpact *
            absoluteScaleValue(ops, inventory.supply.quantityOf(product)).value *
            absoluteScaleValue(ops, exchange.quantity()).value
    }
}
