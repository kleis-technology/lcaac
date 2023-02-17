package ch.kleis.lcaplugin.core.lang

import java.math.BigInteger

class Exponent(upper: Int, lower: Int) {
    val upper: Int
    val lower: Int

    init {
        if (lower <= 0) {
            throw IllegalArgumentException("lower must be positive")
        }
        val q = gcd(upper, lower)
        this.upper = upper / q
        this.lower = lower / q
    }

    private fun gcd(a: Int, b: Int): Int {
        return BigInteger.valueOf(a.toLong())
            .gcd(BigInteger.valueOf(b.toLong()))
            .toInt()
    }

    companion object {
        val ZERO = Exponent(0, 1)
        val ONE = Exponent(1, 1)
    }

    fun isZero(): Boolean {
        return upper == 0
    }

    fun isOne(): Boolean {
        return upper == 1
    }

    fun isNonZero(): Boolean {
        return upper != 0
    }

    fun negate(): Exponent {
        return Exponent(-upper, lower)
    }

    fun add(other: Exponent): Exponent {
        val u = upper * other.lower + lower * other.upper
        val l = lower * other.lower
        return Exponent(u, l)
    }

    fun sub(other: Exponent): Exponent {
        val u = upper * other.lower - lower * other.upper
        val l = lower * other.lower
        return Exponent(u, l)
    }

    fun mul(n: Int): Exponent {
        return Exponent(upper * n, lower)
    }

    fun div(n: Int): Exponent {
        return Exponent(upper, lower * n)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Exponent

        return upper * other.lower == lower * other.upper
    }

    override fun hashCode(): Int {
        val q = Math.floorDiv(upper, lower)
        val r = upper - q * lower
        var result = q
        result = 31 * result + r
        return result
    }
}

class Dimension(elements: Map<String, Exponent>) {
    private val elements: Map<String, Exponent>

    override fun toString(): String {
        return elements.entries.joinToString(".") {
            if (it.value.lower > 1) {
                "${it.key}[${it.value.upper}/${it.value.lower}]"
            } else "${it.key}[${it.value.upper}]"
        }
    }
    
    init {
        this.elements = elements.filter { it.value.isNonZero() }
    }

    companion object {
        val None = Dimension(emptyMap())
    }

    fun isNone(): Boolean {
        return this.elements.isEmpty()
    }

    constructor(name: String) : this(mapOf(Pair(name, Exponent.ONE)))

    fun multiply(other: Dimension): Dimension {
        val es = HashMap<String, Exponent>(elements)
        other.elements.entries.forEach { entry ->
            es[entry.key] = es[entry.key]?.add(entry.value) ?: entry.value
        }
        return Dimension(es)
    }

    fun divide(other: Dimension): Dimension {
        val es = HashMap<String, Exponent>(elements)
        other.elements.entries.forEach { entry ->
            es[entry.key] = es[entry.key]?.sub(entry.value) ?: entry.value.negate()
        }
        return Dimension(es)
    }

    fun pow(n: Int): Dimension {
        val es = HashMap<String, Exponent>()
        elements.entries.forEach { entry ->
            es[entry.key] = entry.value.mul(n)
        }
        return Dimension(es)
    }

    fun root(n: Int): Dimension {
        val es = HashMap<String, Exponent>()
        elements.entries.forEach { entry ->
            es[entry.key] = entry.value.div(n)
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
