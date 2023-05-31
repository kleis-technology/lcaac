package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics
import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.math.DoubleComparator


@optics
data class UnitValue(val symbol: String, val scale: Double, val dimension: Dimension) : Value {
    override fun toString(): String {
        return symbol
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

    companion object

}
