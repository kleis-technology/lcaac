package ch.kleis.lcaac.core.lang.value

import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.EStringLiteral

sealed interface DataValue<Q> : Value<Q>

data class StringValue<Q>(val s: String) : DataValue<Q> {
    override fun toString(): String {
        return s
    }

    fun toEStringLiteral(): EStringLiteral<Q> = EStringLiteral(s)
}

data class QuantityValue<Q>(val amount: Q, val unit: UnitValue<Q>) : DataValue<Q> {
    override fun toString(): String {
        return "$amount ${unit.symbol}"
    }

    fun toEQuantityScale(): EQuantityScale<Q> = EQuantityScale(amount, unit.toEUnitLiteral())
}

data class RecordValue<Q>(val entries: Map<String, DataValue<Q>>) : DataValue<Q> {
    override fun toString(): String {
        return entries.toString()
    }
}
