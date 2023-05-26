package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics
import ch.kleis.lcaplugin.core.lang.Dimension
import kotlin.math.abs

const val ACCEPTABLE_SCALE_GAP = 1E-11

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
        if (scale == 0.0) return abs(other.scale) <= ACCEPTABLE_SCALE_GAP
        if (abs((scale - other.scale) / scale) > ACCEPTABLE_SCALE_GAP) return false

        return true
    }

    override fun hashCode(): Int {
        return 1
    }

    companion object

}
