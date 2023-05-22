package ch.kleis.lcaplugin.core.lang

import arrow.optics.Fold

data class RegisterException(val duplicates: Set<String>) : Exception("$duplicates ${
    if (duplicates.size > 1) {
        "are"
    } else {
        "is"
    }
} already bound")

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

    fun <K> getEntries(optics: Fold<E, K>): Map<K, E> {
        return data.entries.asSequence()
            .flatMap { entry -> optics.getAll(entry.value).map { value -> value to entry.value } }
            // ensure no duplicate by calling reduce as soon as there is a second element in a group
            .groupingBy { it.first }.reduce { key, _, _ -> throw RegisterException(setOf(key.toString())) }
            .asSequence().map { it }.associate { it.key to it.value.second }
    }

    operator fun get(key: String): E? {
        return data[key]
    }

    fun getValues(): Sequence<E> = data.values.asSequence()

    override fun toString(): String {
        return "[register<${registerType}>]"
    }

    override fun equals(other: Any?): Boolean {
        return (this === other) or ((javaClass == other?.javaClass) and (data == (other as Register<*>).data))
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

        val firstConflicts = keys.groupingBy { it }.eachCount()
            .filter { it.value > 1 }
            .map { it.key }
            .take(20)
            .toSet()
        if (firstConflicts.isNotEmpty()) {
            throw RegisterException(firstConflicts.take(10).toSet())
        }
        return Register(
            registerType,
            data.plus(pairs)
        )
    }
}
