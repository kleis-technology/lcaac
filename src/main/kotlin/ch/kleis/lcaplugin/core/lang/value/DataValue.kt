package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.math.QuantityOperations

sealed interface DataValue<Q> : Value<Q>

data class StringValue<Q>(val s: String) : DataValue<Q> {
    override fun toString(): String {
        return s
    }
}

data class QuantityValue<Q>(val amount: Q, val unit: UnitValue<Q>) : DataValue<Q> {
    fun referenceValue(ops: QuantityOperations<Q>): Double {
        with(ops) {
            return toDouble(amount) * unit.scale
        }
    }

    override fun toString(): String {
        return "$amount ${unit.symbol}"
    }
}
