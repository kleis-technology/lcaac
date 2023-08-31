package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.math.Operations

class SupplyMatrix<Q, M>(
    private val observablePorts: IndexedCollection<MatrixColumnIndex<Q>>,
    private val data: M,
    private val ops: Operations<Q, M>,
    ) {
    fun quantityOf(port: MatrixColumnIndex<Q>): QuantityValue<Q> {
        with(ops) {
            val unit = port.referenceUnit()
            val amount = data[0, observablePorts.indexOf(port)]
            return QuantityValue(amount, unit)
        }
    }
}
