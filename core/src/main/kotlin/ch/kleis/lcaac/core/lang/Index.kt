package ch.kleis.lcaac.core.lang

import arrow.optics.Fold

class Index<S, K, E> private constructor(
    private val cachedEntries: Map<S, List<E>>,
) {
    constructor(
        register: Register<K, E>,
        optics: Fold<E, S>
    ) : this(
        register.getEntries(optics),
    )

    fun firstOrNull(s: S): E? {
        return cachedEntries[s]?.firstOrNull()
    }

    fun getAll(s: S): List<E> {
        return cachedEntries[s] ?: emptyList()
    }

    override fun toString(): String {
        return "[index]"
    }
}
