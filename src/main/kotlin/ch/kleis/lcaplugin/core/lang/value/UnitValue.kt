package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics
import ch.kleis.lcaplugin.core.lang.Dimension

@optics
data class UnitValue(val symbol: String, val scale: Double, val dimension: Dimension) : Value {
    override fun toString(): String {
        return symbol
    }

    companion object
}
