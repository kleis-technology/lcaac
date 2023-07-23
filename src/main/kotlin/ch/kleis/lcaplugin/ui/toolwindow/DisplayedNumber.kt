package ch.kleis.lcaplugin.ui.toolwindow

import arrow.core.flatten
import arrow.core.replicate
import kotlin.math.*

class DisplayedNumber(
    value: Double,
    nbSignificantDigits: Int = 3
) {
    private val repr = FloatingPointRepresentation.of(value, nbSignificantDigits)

    override fun toString(): String {
        val isPositive = repr.isPositive
        val digits = repr.digits
        val nbSignificantDigits = repr.nbSignificantDigits
        return when (val positionalExponent = repr.positionalExponent) {
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


