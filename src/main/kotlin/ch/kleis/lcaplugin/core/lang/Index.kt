package ch.kleis.lcaplugin.core.lang

import arrow.optics.PEvery

class Index<K, E> private constructor(
    private val indexType: String,
    private val cachedEntries: Map<K, E>
) {
    constructor(
        register: Register<E>,
        optics: PEvery<E, E, K, K>
    ) : this(register.registerType, register.getEntries(optics))

    companion object {
        internal inline fun <K, reified E> empty(): Index<K, E> {
            return Index(E::class.java.simpleName, emptyMap())
        }
    }

    operator fun get(key: K): E? {
        return cachedEntries[key]
    }

    override fun toString(): String {
        return "[index<${indexType}>]"
    }

}
