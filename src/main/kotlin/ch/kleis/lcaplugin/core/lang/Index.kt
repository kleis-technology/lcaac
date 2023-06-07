package ch.kleis.lcaplugin.core.lang

import arrow.optics.Fold

interface IndexKeySerializer<K> {
    fun serialize(key: K): String
}

class Index<K, E> private constructor(
    private val indexType: String,
    private val indexKeySerializer: IndexKeySerializer<K>,
    private val cachedEntries: Map<String, List<E>>,
    private val optics: Fold<E, K>,
) {
    constructor(
        register: Register<E>,
        indexKeySerializer: IndexKeySerializer<K>,
        optics: Fold<E, K>
    ) : this(
        register.registerType,
        indexKeySerializer,
        register.getEntries(optics).mapKeys { indexKeySerializer.serialize(it.key) },
        optics,
    )

    fun firstOrNull(key: K): E? {
        val h = indexKeySerializer.serialize(key)
        return cachedEntries[h]?.firstOrNull {
            this.optics.firstOrNull(it) == key
        }
    }

    fun getAll(key: K): List<E> {
        return cachedEntries[indexKeySerializer.serialize(key)] ?: emptyList()
    }

    override fun toString(): String {
        return "[index<${indexType}>]"
    }

}
