package ch.kleis.lcaplugin.ui.toolwindow

import arrow.core.replicate
import com.intellij.util.containers.tail
import kotlin.math.*

class DisplayedNumber(
    private val value: Double,
    private val nbSignificantDigits: Int = 3
) {
    private val isPositive: Boolean
    private val digits: List<Int>
    private val positionalExponent: Int

    init {
        fun prepareNonZero(v: Double): Pair<Int, List<Int>> {
            val w = abs(v)
            val positionalExponent = ceil(log10(w)).toInt()
            val shift = nbSignificantDigits - positionalExponent
            val reversedDigits = ArrayList<Int>()
            var u = floor(w * (10.0.pow(shift))).toInt()
            while (u > 0) {
                reversedDigits.add(u.mod(10))
                u /= 10
            }
            val digits = reversedDigits.reversed()

            if (digits.tail().take(nbSignificantDigits - 1).all { it == 9 }) {
                val hd = digits[0]
                if (hd == 9) {
                    val zeros = listOf(0).replicate(nbSignificantDigits - 2).flatten()
                    return positionalExponent to listOf(1, 0) + zeros
                }
                val zeros = listOf(0).replicate(nbSignificantDigits - 1).flatten()
                return positionalExponent to listOf(hd + 1) + zeros
            }

            return positionalExponent to digits
        }

        if (value == 0.0) {
            isPositive = true
            positionalExponent = 1
            digits = IntRange(1, nbSignificantDigits).map { 0 }
        } else {
            val (e, d) = prepareNonZero(value)
            isPositive = value > 0.0
            positionalExponent = e
            digits = d
        }
    }

    fun getExponent(): Int {
        return positionalExponent
    }

    fun getDigits(): List<Int> {
        return digits
    }

    private fun isPowerOf10(): Boolean {
        return digits[0] == 1
            && digits.tail().all { it == 0 }
    }

    private fun renderPowerOf10(): String {
        return when (positionalExponent) {
            -2 -> "0.01"
            -1 -> "0.1"
            0 -> "1"
            1 -> "10"
            2 -> "100"
            else -> "1E$positionalExponent"
        }.let {
            if (isPositive) it else "-$it"
        }
    }

    override fun toString(): String {
        if (value == 0.0) {
            return "0"
        }

        if (isPowerOf10()) {
            return renderPowerOf10()
        }

        val midpoint = when {
            positionalExponent <= -nbSignificantDigits + 1 -> 1
            -nbSignificantDigits + 1 < positionalExponent
                && positionalExponent < nbSignificantDigits -> positionalExponent

            else -> nbSignificantDigits
        }
        val displayedExponent = when {
            -nbSignificantDigits + 1 < positionalExponent
                && positionalExponent < nbSignificantDigits -> 0

            else -> positionalExponent - midpoint
        }

        val head = when {
            midpoint <= 0 -> emptyList()
            else -> digits.subList(0, midpoint)
        }.joinToString("").ifEmpty { "0" }
        val tail = when {
            midpoint <= 0 -> {
                val prefix = listOf(0).replicate(-midpoint).flatten()
                prefix + digits.take(nbSignificantDigits)
            }
            else -> digits.subList(midpoint, nbSignificantDigits)
        }.dropLastWhile { it == 0 }.joinToString("")

        val sign = if (isPositive) "" else "-"
        val e = if (displayedExponent == 0) "" else "E$displayedExponent"
        return if (tail.isEmpty()) "$sign$head$e" else "$sign${head}.$tail$e"
    }
}


