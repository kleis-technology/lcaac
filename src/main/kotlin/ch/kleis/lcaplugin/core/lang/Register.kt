package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException

class Register<E>(
    data: Map<String, E> = HashMap()
) : Map<String, E> {
    private val data = HashMap(data)

    constructor(register: Register<E>) : this(register.data)
    constructor(vararg pairs: Pair<String, E>) : this(mapOf(*pairs))

    companion object {
        fun <E> empty(): Register<E> {
            return Register()
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<String, E>>
        get() = data.entries
    override val keys: MutableSet<String>
        get() = data.keys
    override val size: Int
        get() = data.size
    override val values: MutableCollection<E>
        get() = data.values

    override fun isEmpty(): Boolean {
        return data.isEmpty()
    }

    override fun get(key: String): E? {
        return data[key]
    }

    override fun containsValue(value: E): Boolean {
        return data.containsValue(value)
    }

    override fun containsKey(key: String): Boolean {
        return data.containsKey(key)
    }

    override fun toString(): String {
        return "[register<${this.javaClass.simpleName}>]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Register<*>

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    fun plus(pair: Pair<String, E>): Register<E> {
        val key = pair.first
        if (data.containsKey(key)) {
            throw EvaluatorException("reference $key already bound: $key = ${data[key]}")
        }
        return Register(
            data.plus(pair)
        )
    }

    fun plus(map: Map<String, E>): Register<E> {
        val conflicts = map.keys
            .filter { data.containsKey(it) }
        if (conflicts.isNotEmpty()) {
            throw EvaluatorException("$conflicts are already bound")
        }
        return Register(
            data.plus(map)
        )
    }

    fun plus(pairs: Iterable<Pair<String, E>>): Register<E> {
        val keys = data.keys.toList()
            .plus(pairs.map { it.first })
        val conflicts = keys.groupingBy { it }.eachCount()
            .filter { it.value > 1 }
            .map { it.key }
            .toSet()
        if (conflicts.isNotEmpty()) {
            throw EvaluatorException("$conflicts are already bound")
        }
        return Register(
            data.plus(pairs)
        )
    }
}
