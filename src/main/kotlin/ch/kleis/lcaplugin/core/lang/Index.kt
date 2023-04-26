package ch.kleis.lcaplugin.core.lang

import arrow.optics.PEvery
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException

class Index<E> private constructor(
    private val indexType: String,
    private val cachedEntries: Map<String, E>
) {
    constructor(
        register: Register<E>,
        optics: PEvery<E, E, String, String>
    ) : this(register.registerType, register.entries.asSequence()
        .flatMap { entry -> optics.getAll(entry.value).map { value -> value to entry.value } }
        // ensure no duplicate by calling reduce as soon as there is a second element in a group
        .groupingBy { it.first }.reduce {key, _, _ -> throw EvaluatorException("$key is already bound") }
        .asSequence().map { it }.associate { it.key to it.value.second })

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
