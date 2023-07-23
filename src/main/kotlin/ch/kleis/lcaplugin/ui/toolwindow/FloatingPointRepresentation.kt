package ch.kleis.lcaplugin.ui.toolwindow

import arrow.core.flatten
import arrow.core.replicate
import com.intellij.util.containers.tail
import kotlin.math.*

data class FloatingPointRepresentation(
    val isPositive: Boolean,
    val digits: List<Int>,
    val positionalExponent: Int,
    val nbSignificantDigits: Int,
) {
    companion object {
        fun of(value: Double, nbSignificantDigits: Int = 3): FloatingPointRepresentation {
            if (value == 0.0) {
                return FloatingPointRepresentation(
                    true,
                    IntRange(1, nbSignificantDigits).map { 0 },
                    0,
                    nbSignificantDigits
                )
            }
            val isPositive = value > 0.0
            val w = abs(value)
            val e = ceil(log10(w)).toInt()
            val positionalExponent =
                if (w == 10.0.pow(e)) e
                else e - 1
            val shift = nbSignificantDigits - positionalExponent
            val reversedDigits = ArrayList<Int>()
            var u = floor(w * (10.0.pow(shift))).toInt()
            while (u > 0) {
                reversedDigits.add(u.mod(10))
                u /= 10
            }
            val digits = reversedDigits.reversed().take(nbSignificantDigits)

            if (digits.tail().take(nbSignificantDigits - 1).all { it == 9 }) {
                val hd = digits[0]
                if (hd == 9) {
                    val zeros = listOf(0).replicate(nbSignificantDigits - 2).flatten()
                    return FloatingPointRepresentation(
                        isPositive,
                        listOf(1, 0) + zeros,
                        positionalExponent + 1,
                        nbSignificantDigits
                    )
                }
                val zeros = listOf(0).replicate(nbSignificantDigits - 1).flatten()
                return FloatingPointRepresentation(
                    isPositive,
                    listOf(hd + 1) + zeros,
                    positionalExponent,
                    nbSignificantDigits
                )
            }
            return FloatingPointRepresentation(isPositive, digits, positionalExponent, nbSignificantDigits)
        }
    }
}
