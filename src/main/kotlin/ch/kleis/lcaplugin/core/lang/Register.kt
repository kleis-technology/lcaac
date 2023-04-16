package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException


class Register<E> private constructor(
    internal val registerType: String,
    private val data: Map<String, E> = HashMap()
) {
    constructor(register: Register<E>) : this(register.registerType, register.data)

    companion object {
        internal inline fun <reified E> empty(): Register<E> {
            return Register(E::class.java.simpleName)
        }

        internal inline fun <reified E> from(data: Map<String, E>): Register<E> {
            return Register(E::class.java.simpleName, data)
        }
    }

    @Deprecated(message = "should not be opened to other class")
    val entries: Set<Map.Entry<String, E>>
        get() = data.entries

    operator fun get(key: String): E? {
        return data[key]
    }

    override fun toString(): String {
        return "[register<${registerType}>]"
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

    fun plus(map: Map<String, E>): Register<E> {
        return plus(map.map { it.key to it.value })
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
        return Register(registerType,
            data.plus(pairs)
        )
    }
}
