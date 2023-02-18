package ch.kleis.lcaplugin.core.lang

class Dimension(elements: Map<String, Double>) {
    private val elements: Map<String, Double>

    override fun toString(): String {
        return elements.entries.joinToString(".") {
            "${it.key}[${it.value}]"
        }
    }

    init {
        this.elements = elements.filter { it.value != 0.0 }
    }

    companion object {
        val None = Dimension(emptyMap())
        fun of(name: String): Dimension {
            return if (name == "none") None else Dimension(mapOf(Pair(name, 1.0)))
        }
    }

    fun isNone(): Boolean {
        return this.elements.isEmpty()
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
                ?: entry.value
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
