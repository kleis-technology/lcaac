package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.math.QuantityOperations
import ch.kleis.lcaplugin.core.matrix.impl.Matrix

class SupplyMatrix<Q>(
    val observablePorts: IndexedCollection<MatrixColumnIndex<Q>>,
    val matrix: Matrix,
    private val ops: QuantityOperations<Q>,
    ) {
    fun quantityOf(port: MatrixColumnIndex<Q>): QuantityValue<Q> {
        val unit = port.referenceUnit()
        val amount = matrix.value(0, observablePorts.indexOf(port))
        return QuantityValue(ops.pure(amount), unit)
    }
}
