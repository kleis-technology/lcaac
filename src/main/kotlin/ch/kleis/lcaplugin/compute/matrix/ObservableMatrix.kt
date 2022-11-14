package ch.kleis.lcaplugin.compute.matrix

import ch.kleis.lcaplugin.compute.matrix.impl.Matrix
import ch.kleis.lcaplugin.compute.matrix.impl.MatrixFactory
import ch.kleis.lcaplugin.compute.model.IntermediaryFlow
import ch.kleis.lcaplugin.compute.model.Process

class ObservableMatrix(private val processes: IndexedCollection<Process>, private val observableFlows: IndexedCollection<IntermediaryFlow<*>>) {
    val matrix: Matrix = MatrixFactory.INSTANCE.zero(processes.size(), observableFlows.size())

    init {
        processes.getElements().forEach { process ->
            val allocationFactor = 1.0
            val row = processes.indexOf(process)
            process.products
                .filter { observableFlows.contains(it.flow) }
                .forEach { product ->
                val col = observableFlows.indexOf(product.flow)
                matrix.add(row, col, product.quantity.referenceValue())
            }

            process.inputs
                .filter { observableFlows.contains(it.flow) }
                .forEach { input ->
                    val col = observableFlows.indexOf(input.flow)
                    matrix.add(row, col, - allocationFactor * input.quantity.referenceValue())
                }
        }
    }
}
