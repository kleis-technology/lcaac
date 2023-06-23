package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.math.DoubleComparator


data class UnitValue(val symbol: UnitSymbol, val scale: Double, val dimension: Dimension) : Value {
    override fun toString(): String {
        return symbol.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnitValue

        if (dimension != other.dimension) return false
        return DoubleComparator.nzEquals(scale, other.scale)
    }

    override fun hashCode(): Int {
        return 1
    }
}
