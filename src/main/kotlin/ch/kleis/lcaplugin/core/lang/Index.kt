package ch.kleis.lcaplugin.core.lang

import arrow.optics.PEvery

class Index<E> private constructor(
    private val indexType: String,
    private val cachedEntries: Map<String, E>
) {
    constructor(
        register: Register<E>,
        optics: PEvery<E, E, String, String>
    ) : this(register.registerType, register.getEntries(optics))

    companion object {
        internal inline fun <reified E> empty(): Index<E> {
            return Index(E::class.java.simpleName, emptyMap())
        }
    }

    operator fun get(key: String): E? {
        return cachedEntries[key]
    }

    override fun toString(): String {
        return "[index<${indexType}>]"
    }

}
