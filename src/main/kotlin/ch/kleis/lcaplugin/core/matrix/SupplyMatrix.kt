package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.matrix.impl.Matrix

class SupplyMatrix(
    val observablePorts: IndexedCollection<MatrixColumnIndex>,
    val matrix: Matrix
    ) {
    fun quantityOf(port: MatrixColumnIndex): QuantityValue {
        val unit = port.referenceUnit()
        val amount = matrix.value(0, observablePorts.indexOf(port))
        return QuantityValue(amount, unit)
    }
}
