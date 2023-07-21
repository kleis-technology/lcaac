package ch.kleis.lcaplugin.ui.toolwindow

import kotlin.math.*

class DisplayedNumber(
    private val value: Double,
    private val nbSignificantDigits: Int = 3
) {
    private val isPositive: Boolean
    private val digits: List<Int>
    private val exponent: Int

    init {
        fun prepareNonZero(v: Double): Pair<Int, List<Int>> {
            val w = abs(v)
            val exponent = ceil(log10(w)).toInt()
            val shift = nbSignificantDigits - exponent
            val digits = ArrayList<Int>()
            var u = floor(w * (10.0.pow(shift))).toInt()
            while (u > 0) {
                digits.add(u.mod(10))
                u /= 10
            }
            return exponent to digits.reversed()
        }

        if (value == 0.0) {
            isPositive = true
            exponent = 1
            digits = IntRange(1, nbSignificantDigits).map { 0 }
        } else {
            val (e, d) = prepareNonZero(value)
            isPositive = value > 0.0
            exponent = e
            digits = d
        }
    }

    fun getExponent(): Int {
        return exponent
    }

    fun getDigits(): List<Int> {
        return digits
    }

    override fun toString(): String {
        if (value == 0.0) {
            return "0"
        }
        if (value == 1.0) {
            return "1"
        }
        if (value == -1.0) {
            return "-1"
        }

        val midpoint = when {
            exponent < 0 -> 1
            exponent <= nbSignificantDigits -> exponent
            else -> nbSignificantDigits
        }
        val displayedExponent = exponent - midpoint

        val head = digits.subList(0, midpoint).joinToString("")
            .ifEmpty { "0" }
        val tail = digits.subList(midpoint, nbSignificantDigits)
            .dropLastWhile { it == 0 }
            .joinToString("")

        val sign = if (isPositive) "" else "-"
        val e = if (displayedExponent == 0) "" else "E$displayedExponent"
        return if (tail.isEmpty()) "$sign$head$e" else "$sign${head}.$tail$e"
    }
}


