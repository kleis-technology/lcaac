package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.math.QuantityOperations

internal fun <Q> absoluteScaleValue(ops: QuantityOperations<Q>, quantityValue: QuantityValue<Q>): Q {
    with(ops) {
        return quantityValue.amount * pure(quantityValue.unit.scale)
    }
}
