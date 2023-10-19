package ch.kleis.lcaac.core.lang

import arrow.optics.Fold
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.lang.expression.ESubstanceCharacterization

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

data class DataKey(
    val name: String,
) {
    override fun toString() = name
}
typealias DataRegister<Q> = Register<DataKey, DataExpression<Q>>

data class DimensionKey(
    val name: String,
){
    override fun toString() = name
}
typealias DimensionRegister = Register<DimensionKey, Dimension>

data class ProcessKey(
    val name: String,
    val labels: Map<String, String> = emptyMap(),
){
    override fun toString(): String {
        return name + labels.entries.joinToString(", ") { "${it.key}=${it.value}" }
            .let { if (it.isNotEmpty()) "{$it}" else "" }
    }
}
typealias ProcessTemplateRegister<Q> = Register<ProcessKey, EProcessTemplate<Q>>

data class SubstanceKey(
    val name: String,
    val type: String? = null,
    val compartment: String? = null,
    val subCompartment: String? = null,
) {
    override fun toString(): String {
        return name + listOfNotNull(
            type?.let { "type=$it" },
            compartment?.let { "compartment=$it" },
            subCompartment?.let { "sub_compartment=$it" },
        ).joinToString(", ").let { if (it.isNotEmpty()) "($it)"  else ""}
    }
}

typealias SubstanceCharacterizationRegister<Q> = Register<SubstanceKey, ESubstanceCharacterization<Q>>
