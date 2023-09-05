package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.math.DoubleComparator
import kotlin.math.pow


data class UnitValue<Q>(val symbol: UnitSymbol, val scale: Double, val dimension: Dimension) : Value<Q> {
    companion object {
        fun <Q> none() = UnitValue<Q>(UnitSymbol.None, 1.0, Dimension.None)
    }

    override fun toString(): String {
        return symbol.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        @Suppress("UNCHECKED_CAST")
        other as UnitValue<Q>

        if (dimension != other.dimension) return false
        return DoubleComparator.nzEquals(scale, other.scale)
    }

    override fun hashCode(): Int {
        return 1
    }

    operator fun times(other: UnitValue<Q>): UnitValue<Q> {
        return UnitValue(symbol.multiply(other.symbol), scale * other.scale, dimension.multiply(other.dimension))
    }

    operator fun div(other: UnitValue<Q>): UnitValue<Q> {
        return UnitValue(symbol.divide(other.symbol), scale / other.scale, dimension.divide(other.dimension))
    }

    fun pow(n: Double): UnitValue<Q> {
        return UnitValue(symbol.pow(n), scale.pow(n), dimension.pow(n))
    }

    fun scale(s: Double): UnitValue<Q> {
        return UnitValue(symbol.scale(s), s * scale, dimension)
    }
}
