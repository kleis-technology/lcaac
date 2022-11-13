package ch.kleis.lcaplugin.compute.matrix

import ch.kleis.lcaplugin.compute.matrix.impl.Matrix
import ch.kleis.lcaplugin.compute.matrix.impl.MatrixFactory
import ch.kleis.lcaplugin.compute.model.IntermediaryFlow
import ch.kleis.lcaplugin.compute.model.Process

class ControllableMatrix(private val processes: IndexedCollection<Process>, private val controllableFlows: IndexedCollection<IntermediaryFlow>) {
    val matrix: Matrix = MatrixFactory.INSTANCE.zero(processes.size(), controllableFlows.size())

    init {
        processes.getElements().forEach { process ->
            val allocationFactor = 1.0
            val row = processes.indexOf(process)
            process.products
                .filter { controllableFlows.contains(it.flow) }
                .forEach { product ->
                val col = controllableFlows.indexOf(product.flow)
                matrix.add(row, col, product.quantity.referenceValue())
            }

            process.inputs
                .filter { controllableFlows.contains(it.flow) }
                .forEach { input ->
                    val col = controllableFlows.indexOf(input.flow)
                    matrix.add(row, col, - allocationFactor * input.quantity.referenceValue())
                }
        }
    }
}
