package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.math.QuantityOperations
import ch.kleis.lcaplugin.core.matrix.impl.Matrix
import ch.kleis.lcaplugin.core.matrix.impl.MatrixFactory

class DemandMatrix<Q>(
    targetProcess: ProcessValue<Q>,
    observablePorts: IndexedCollection<MatrixColumnIndex<Q>>,
    ops: QuantityOperations<Q>,
) {
    val matrix: Matrix = MatrixFactory.INSTANCE.zero(1, observablePorts.size())

    init {
        targetProcess.products.forEach {
            val col = observablePorts.indexOf(it.product)
            matrix.add(0, col, it.quantity.referenceValue(ops))
        }
    }
}
