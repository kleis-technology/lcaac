package ch.kleis.lcaac.core.lang.dimension

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.math.DoubleComparator
import kotlin.math.pow

class UnitSymbol(
    elements: Map<String, Double>,
    scale: Double = 1.0,
) {
    private val scale: Double
    private val elements: Map<String, Double>

    override fun toString(): String {
        val s = if (scale == 1.0) null else scale.toString()
        val symbol = elements.entries.joinToString(".") {
            simpleDimToString(it)
        }
        return listOfNotNull(
            s,
            symbol
        ).joinToString(" ")
    }

    init {
        if (scale == 0.0) {
            throw EvaluatorException("scale is zero")
        }
        this.scale = scale
        this.elements = elements.filter { it.value != 0.0 }
    }

    companion object {
        fun of(name: String): UnitSymbol {
            return UnitSymbol(mapOf(name to 1.0))
        }
        val None = UnitSymbol(emptyMap())
    }

    fun multiply(other: UnitSymbol): UnitSymbol {
        return UnitSymbol(multiply(elements, other.elements), scale * other.scale)
    }

    fun divide(other: UnitSymbol): UnitSymbol {
        return UnitSymbol(divide(elements, other.elements), scale / other.scale)
    }

    fun pow(n: Double): UnitSymbol {
        return UnitSymbol(pow(elements, n), scale.pow(n))
    }

    fun scale(s: Double): UnitSymbol {
        return UnitSymbol(elements, scale * s)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnitSymbol

        if (!DoubleComparator.nzEquals(scale, other.scale)) return false
        return elements == other.elements
    }

    override fun hashCode(): Int {
        var result = scale.hashCode()
        result = 31 * result + elements.hashCode()
        return result
    }
}
