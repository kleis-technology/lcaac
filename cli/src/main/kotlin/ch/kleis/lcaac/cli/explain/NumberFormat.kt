package ch.kleis.lcaac.cli.explain

import java.math.BigDecimal
import java.math.MathContext
import java.util.*
import kotlin.math.abs

/**
 * Prints numbers with [precision] significant digits. Whole numbers are printed exactly: rounding
 * 34359738368 to 34359700000 would hide what is being computed.
 */
class NumberFormat(private val precision: Int = 6) {
    fun format(value: Double): String {
        if (value == 0.0) return "0"
        if (value.isNaN() || value.isInfinite()) return value.toString()
        val a = abs(value)
        if (a < 1e15 && value == Math.rint(value)) return BigDecimal(value).toPlainString()
        if (a >= 1e-4 && a < 1e12) {
            return BigDecimal(value).round(MathContext(precision)).stripTrailingZeros().toPlainString()
        }
        val (mantissa, exponent) = String.format(Locale.ROOT, "%.${precision - 1}e", value).split("e")
        val trimmed = if (mantissa.contains('.')) mantissa.trimEnd('0').trimEnd('.') else mantissa
        return "${trimmed}e${exponent.toInt()}"
    }
}
