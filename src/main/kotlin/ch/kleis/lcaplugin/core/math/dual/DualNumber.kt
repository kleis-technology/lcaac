package ch.kleis.lcaplugin.core.math.dual

import org.jetbrains.kotlinx.multik.ndarray.data.D1Array


data class DualNumber(
    val zeroth: Double,
    val first: D1Array<Double>,
)
