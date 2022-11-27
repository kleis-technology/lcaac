package ch.kleis.lcaplugin.compute.matrix

import ch.kleis.lcaplugin.compute.matrix.impl.Matrix
import ch.kleis.lcaplugin.compute.matrix.impl.MatrixFactory
import ch.kleis.lcaplugin.compute.model.Flow
import ch.kleis.lcaplugin.compute.model.UnitProcess

class ControllableMatrix(private val processes: IndexedCollection<UnitProcess>, private val controllableFlows: IndexedCollection<Flow<*>>) {
    val matrix: Matrix = MatrixFactory.INSTANCE.zero(processes.size(), controllableFlows.size())

    init {
        processes.getElements().forEach { process ->
            val row = processes.indexOf(process)
            process.outputs
                .filter { controllableFlows.contains(it.flow) }
                .forEach { product ->
                val col = controllableFlows.indexOf(product.flow)
                matrix.add(row, col, product.quantity.referenceValue())
            }

            process.inputs
                .filter { controllableFlows.contains(it.flow) }
                .forEach { input ->
                    val col = controllableFlows.indexOf(input.flow)
                    matrix.add(row, col, input.quantity.referenceValue())
                }
        }
    }
}
