package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.VProcess
import ch.kleis.lcaplugin.core.lang.VProduct
import ch.kleis.lcaplugin.core.matrix.impl.Matrix
import ch.kleis.lcaplugin.core.matrix.impl.MatrixFactory


class ObservableMatrix(
    private val processes: IndexedCollection<VProcess>,
    private val observableProducts: IndexedCollection<VProduct>,
) {
    val matrix: Matrix = MatrixFactory.INSTANCE.zero(processes.size(), observableProducts.size())

    init {
        processes.getElements().forEach { process ->
            val row = processes.indexOf(process)
            process.exchanges
                .filter { observableProducts.contains(it.product) }
                .forEach { product ->
                    val col = observableProducts.indexOf(product.product)
                    matrix.add(row, col, product.quantity.referenceValue())
                }
        }
    }
}
