package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics

sealed interface DataValue : Value

@optics
data class StringValue(val s : String): DataValue {
    companion object
}

@optics
data class QuantityValue(val amount: Double, val unit: UnitValue) : DataValue {
    fun referenceValue(): Double {
        return amount * unit.scale
    }

    override fun toString(): String {
        return "$amount ${unit.symbol}"
    }

    companion object
}
