package ch.kleis.lcaplugin.core.math

import kotlin.math.absoluteValue
import kotlin.math.pow

class DoubleComparator {
    companion object {
        private val machineEpsilon = 2.0.pow(-53.0)
        private val acceptableRelativeError = 8 * machineEpsilon
        fun nzEquals(a: Double, b: Double): Boolean {
            return (a - b).absoluteValue / maxOf(a.absoluteValue, b.absoluteValue) <= acceptableRelativeError
        }
    }
}
