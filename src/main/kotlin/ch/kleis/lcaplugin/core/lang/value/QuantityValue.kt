package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics

@optics
data class QuantityValue(val amount: Double, val unit: UnitValue) : Value {
    fun referenceValue(): Double {
        return amount * unit.scale
    }

    override fun toString(): String {
        return "$amount ${unit.symbol}"
    }

    companion object
}
