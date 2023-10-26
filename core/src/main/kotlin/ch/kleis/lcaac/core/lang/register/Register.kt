package ch.kleis.lcaac.core.lang.register

import arrow.optics.Fold

class Register<K, E> (
    private val data: Map<K, E> = HashMap()
) {
    constructor(register: Register<K, E>) : this(register.data)

    companion object {
        inline fun <reified K, reified E> empty(): Register<K, E> {
            return Register()
        }

        internal inline fun <reified K, reified E> from(data: Map<K, E>): Register<K, E> {
            return Register(data)
        }
    }

    fun <S> getEntries(optics: Fold<E, S>): Map<S, List<E>> {
        return data.entries.asSequence()
            .flatMap { entry -> optics.getAll(entry.value).map { value -> value to entry.value } }
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })
    }

    operator fun get(key: K): E? {
        return data[key]
    }

    override fun toString(): String {
        return "[register]"
    }

    override fun equals(other: Any?): Boolean {
        return (this === other) or ((javaClass == other?.javaClass) and (data == (other as Register<*, *>).data))
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    fun plus(map: Map<K, E>): Register<K, E> {
        return plus(map.map { it.key to it.value })
    }

    fun plus(pairs: Iterable<Pair<K, E>>): Register<K, E> {
        val keys = data.keys.toList()
            .plus(pairs.map { it.first })

        val firstConflicts = keys.groupingBy { it }.eachCount()
            .filter { it.value > 1 }
            .map { it.key }
            .take(20)
            .toSet()
        if (firstConflicts.isNotEmpty()) {
            throw RegisterException(firstConflicts.take(10).map { it.toString() }.toSet())
        }
        return Register(
            data.plus(pairs)
        )
    }
}

