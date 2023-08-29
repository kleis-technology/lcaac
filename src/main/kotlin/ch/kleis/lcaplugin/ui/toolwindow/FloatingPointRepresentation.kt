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
    val original: Double,
    val suffix: String? = null,
) : Comparable<FloatingPointRepresentation> {
    companion object {
        fun of(value: Double, nbSignificantDigits: Int = 3, suffix: String? = null): FloatingPointRepresentation {
            if (value == 0.0) {
                return FloatingPointRepresentation(
                    true,
                    IntRange(1, nbSignificantDigits).map { 0 },
                    0,
                    nbSignificantDigits,
                    value,
                    suffix,
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
                        nbSignificantDigits,
                        value,
                        suffix,
                    )
                }
                val zeros = listOf(0).replicate(nbSignificantDigits - 1).flatten()
                return FloatingPointRepresentation(
                    isPositive,
                    listOf(hd + 1) + zeros,
                    positionalExponent,
                    nbSignificantDigits,
                    value,
                    suffix,
                )
            }
            return FloatingPointRepresentation(isPositive, digits, positionalExponent, nbSignificantDigits, value, suffix)
        }
    }

    override fun compareTo(other: FloatingPointRepresentation): Int {
        return original.compareTo(other.original)
    }

    override fun toString(): String {
        val s = strRep()
        return suffix?.let { "$s$it" } ?: s
    }

    private fun strRep(): String {
        return when (positionalExponent) {
            in -2..-1 -> {
                val hd = listOf(0).replicate(-positionalExponent - 1).flatten()
                    .joinToString("")
                    .let { "0.$it" }
                val tl = digits.take(nbSignificantDigits - positionalExponent + 1)
                    .dropLastWhile { it == 0 }
                    .joinToString("")
                val sign = if (isPositive) "" else "-"
                "$sign$hd$tl"
            }

            in 0..2 -> {
                val midpoint = min(positionalExponent + 1, nbSignificantDigits)
                val hd = digits.subList(0, midpoint)
                    .joinToString("")
                val tl = digits.subList(midpoint, nbSignificantDigits)
                    .dropLastWhile { it == 0 }
                    .joinToString("")
                    .let { if (it.isEmpty()) "" else ".$it" }
                val sign = if (isPositive) "" else "-"
                "$sign$hd$tl"
            }

            else -> {
                val hd = digits.subList(0, 1)
                    .joinToString("")
                val tl = digits.subList(1, nbSignificantDigits)
                    .dropLastWhile { it == 0 }
                    .joinToString("")
                    .let { if (it.isEmpty()) "" else ".$it" }
                val sign = if (isPositive) "" else "-"
                val e = "E$positionalExponent"
                "$sign$hd$tl$e"
            }
        }
    }
}
