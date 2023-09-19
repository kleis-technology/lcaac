package ch.kleis.lcaac.core.lang.dimension

import ch.kleis.lcaac.core.lang.value.UnitValue
import kotlin.math.absoluteValue

class Dimension(
    elements: Map<String, Double>,
) {
    private val elements: Map<String, Double>

    override fun toString(): String {
        if (elements.isEmpty()) {
            return "none"
        }
        return elements.entries.joinToString(".") {
            simpleDimToString(it)
        }
    }

    init {
        val thresholdToZero = 1e-20
        this.elements = elements.filter { it.value.absoluteValue > thresholdToZero }
    }

    companion object {
        val None = Dimension(emptyMap())
        fun of(name: String): Dimension {
            return if (name == "none") None else Dimension(mapOf(Pair(name, 1.0)))
        }

        fun of(name: String, power: Int): Dimension {
            return if (name == "none") None else Dimension(mapOf(Pair(name, power.toDouble())))
        }
    }

    fun <Q> getDefaultUnitValue(): UnitValue<Q> {
        return UnitValue(UnitSymbol(elements), 1.0, this)
    }

    fun multiply(other: Dimension): Dimension {
        return Dimension(multiply(elements, other.elements))
    }

    fun divide(other: Dimension): Dimension {
        return Dimension(divide(elements, other.elements))
    }

    fun pow(n: Double): Dimension {
        return Dimension(pow(elements, n))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dimension

        return elements == other.elements
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }
}


