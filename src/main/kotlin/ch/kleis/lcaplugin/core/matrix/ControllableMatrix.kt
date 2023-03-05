package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang_obsolete.VProcess
import ch.kleis.lcaplugin.core.lang_obsolete.VProduct
import ch.kleis.lcaplugin.core.matrix.impl.Matrix
import ch.kleis.lcaplugin.core.matrix.impl.MatrixFactory

class ControllableMatrix(
    private val processes: IndexedCollection<VProcess>,
    private val controllableProducts: IndexedCollection<VProduct>,
){
    val matrix: Matrix = MatrixFactory.INSTANCE.zero(processes.size(), controllableProducts.size())
    init {
        processes.getElements().forEach { process ->
            val row = processes.indexOf(process)
            process.exchanges
                .filter { controllableProducts.contains(it.product) }
                .forEach { product ->
                    val col = controllableProducts.indexOf(product.product)
                    matrix.add(row, col, product.quantity.referenceValue())
                }
        }
    }
}
