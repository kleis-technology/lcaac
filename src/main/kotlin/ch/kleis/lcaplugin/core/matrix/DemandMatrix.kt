package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.math.Operations

class DemandMatrix<Q, M>(
    targetProcess: ProcessValue<Q>,
    observablePorts: IndexedCollection<MatrixColumnIndex<Q>>,
    ops: Operations<Q, M>,
) {
    val data: M = ops.zeros(1, observablePorts.size())

    init {
        with(ops) {
            targetProcess.products.forEach {
                val col = observablePorts.indexOf(it.product)
                data[0, col] = data[0, col] + absoluteScaleValue(ops, it.quantity)
            }
        }
    }
}
