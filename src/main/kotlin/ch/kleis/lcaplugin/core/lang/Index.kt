package ch.kleis.lcaplugin.core.lang

import arrow.optics.Fold

interface IndexKeySerializer<K> {
    fun serialize(key: K): String
}

class Index<K, E> private constructor(
        private val indexType: String,
        private val indexKeySerializer: IndexKeySerializer<K>,
        private val cachedEntries: Map<String, E>
) {
    constructor(
            register: Register<E>,
            indexKeySerializer: IndexKeySerializer<K>,
            optics: Fold<E, K>
    ) : this(
            register.registerType,
            indexKeySerializer,
            register.getEntries(optics).mapKeys { indexKeySerializer.serialize(it.key) },
    )

    operator fun get(key: K): E? {
        return cachedEntries[indexKeySerializer.serialize(key)]
    }

    override fun toString(): String {
        return "[index<${indexType}>]"
    }

}
