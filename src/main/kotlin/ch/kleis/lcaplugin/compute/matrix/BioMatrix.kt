package ch.kleis.lcaplugin.compute.matrix

import ch.kleis.lcaplugin.compute.model.ElementaryFlow
import ch.kleis.lcaplugin.compute.model.Process
import ch.kleis.lcaplugin.compute.matrix.impl.MatrixFactory
import ch.kleis.lcaplugin.compute.matrix.impl.Matrix

class BioMatrix(private val processes: IndexedCollection<Process>, private val elementaryFlows: IndexedCollection<ElementaryFlow<*>>) {
    val matrix: Matrix = MatrixFactory.INSTANCE.zero(processes.size(), elementaryFlows.size())

    init {
        processes.getElements().forEach { process ->
            val row = processes.indexOf(process)
            process.emissions
                .filter { elementaryFlows.contains(it.flow) }
                .forEach { emission ->
                    val col = elementaryFlows.indexOf(emission.flow)
                    matrix.add(row, col, emission.quantity.referenceValue())
                }

            process.resources
                .filter { elementaryFlows.contains(it.flow) }
                .forEach { input ->
                    val col = elementaryFlows.indexOf(input.flow)
                    matrix.add(row, col, - input.quantity.referenceValue())
                }
        }

    }
}
