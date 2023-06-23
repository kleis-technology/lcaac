package ch.kleis.lcaplugin.core.lang.value

sealed interface DataValue : Value

data class StringValue(val s: String) : DataValue {
    override fun toString(): String {
        return s
    }
}

data class QuantityValue(val amount: Double, val unit: UnitValue) : DataValue {
    fun referenceValue(): Double {
        return amount * unit.scale
    }

    override fun toString(): String {
        return "$amount ${unit.symbol}"
    }
}
