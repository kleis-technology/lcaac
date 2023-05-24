package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.value.UnitValue
import kotlin.math.round

class Dimension(elements: Map<String, Double>) {
    private val elements: Map<String, Double>

    override fun toString(): String {
        return elements.entries.joinToString(".") {
            simpleDimToString(it)
        }
    }

    private fun simpleDimToString(basic: Map.Entry<String, Double>): String {
        return if (basic.value == 1.0) {
            basic.key
        } else {
            val power =
                if (basic.value == round(basic.value)) {
                    toPower(String.format("%d", basic.value.toLong()))
                } else {
                    "^[${basic.value}]"
                }
            "${basic.key}$power"
        }
    }

    private fun toPower(f: String): String? {
        return f.map { convert(it) }
            .reduce { strAcc, char ->
                strAcc?.let { str ->
                    char?.let { c ->
                        str.plus(c)
                    }
                }
            }
    }

    private fun convert(c: Char): String? {
        val result: Int? = when (c) {
            '0' -> 0x2070
            '1' -> 0x00B9
            '2' -> 0x00B2
            '3' -> 0x00B3
            in '4'..'9' -> 0x2070 + (c.code - 48)
            '.' -> 0x02D9
            '-' -> 0x207B
            else -> null
        }
        return result?.let { Character.toString(it) }
    }

    init {
        this.elements = elements.filter { it.value != 0.0 }
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

    fun getDefaultUnitValue(): UnitValue {
        return UnitValue("default($this)", 1.0, this)
    }

    fun multiply(other: Dimension): Dimension {
        val es = HashMap<String, Double>(elements)
        other.elements.entries.forEach { entry ->
            es[entry.key] = es[entry.key]?.let { it + entry.value }
                ?: entry.value
        }
        return Dimension(es)
    }

    fun divide(other: Dimension): Dimension {
        val es = HashMap<String, Double>(elements)
        other.elements.entries.forEach { entry ->
            es[entry.key] = es[entry.key]?.let { it - entry.value }
                ?: (-entry.value)
        }
        return Dimension(es)
    }

    fun pow(n: Double): Dimension {
        val es = HashMap<String, Double>()
        elements.entries.forEach { entry ->
            es[entry.key] = n * entry.value
        }
        return Dimension(es)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dimension

        if (elements != other.elements) return false

        return true
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }


}
