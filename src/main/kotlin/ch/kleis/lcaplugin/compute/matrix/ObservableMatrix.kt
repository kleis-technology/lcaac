package ch.kleis.lcaplugin.compute.matrix

import ch.kleis.lcaplugin.compute.matrix.impl.Matrix
import ch.kleis.lcaplugin.compute.matrix.impl.MatrixFactory
import ch.kleis.lcaplugin.compute.model.Flow
import ch.kleis.lcaplugin.compute.model.UnitProcess

class ObservableMatrix(private val processes: IndexedCollection<UnitProcess>, private val observableFlows: IndexedCollection<Flow<*>>) {
    val matrix: Matrix = MatrixFactory.INSTANCE.zero(processes.size(), observableFlows.size())

    init {
        processes.getElements().forEach { process ->
            val row = processes.indexOf(process)
            process.outputs
                .filter { observableFlows.contains(it.flow) }
                .forEach { product ->
                val col = observableFlows.indexOf(product.flow)
                matrix.add(row, col, product.quantity.referenceValue())
            }

            process.inputs
                .filter { observableFlows.contains(it.flow) }
                .forEach { input ->
                    val col = observableFlows.indexOf(input.flow)
                    matrix.add(row, col, -input.quantity.referenceValue())
                }
        }
    }
}
