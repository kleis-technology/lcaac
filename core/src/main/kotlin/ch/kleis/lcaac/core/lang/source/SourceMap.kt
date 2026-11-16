package ch.kleis.lcaac.core.lang.source

import ch.kleis.lcaac.core.lang.expression.BlockExpression
import ch.kleis.lcaac.core.lang.expression.EBlockEntry
import ch.kleis.lcaac.core.lang.expression.EBlockForEach
import ch.kleis.lcaac.core.lang.expression.EProcess
import ch.kleis.lcaac.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.register.SubstanceKey

/**
 * Where a definition is written. Lines and columns start at 1.
 */
data class SourceLocation(
    val source: String,
    val line: Int,
    val column: Int,
) {
    override fun toString(): String = "$source:$line:$column"
}

/**
 * Where an exchange or a for_each block sits in a process, as written in the source:
 * the section, then the index in that section, then the index in each enclosing for_each body.
 *
 * Indices count the entries of all the blocks of a section in order, e.g. two `inputs { }` blocks
 * are numbered as one list. The biosphere section lists emissions, then resources, then land use.
 */
data class BlockPath(
    val section: Section,
    val indices: List<Int>,
) {
    fun child(index: Int): BlockPath = copy(indices = indices + index)

    companion object {
        fun of(section: Section, index: Int): BlockPath = BlockPath(section, listOf(index))
    }
}

enum class Section { PRODUCTS, INPUTS, BIOSPHERE, IMPACTS }

/**
 * Stable address of a definition, independent of how the expressions are later transformed.
 */
sealed interface SourcePath {
    /** A definition that contains exchanges: a process, or a substance with its impacts. */
    sealed interface ExchangeOwner : SourcePath

    data class Process(val process: ProcessKey) : ExchangeOwner
    data class Substance(val substance: SubstanceKey) : ExchangeOwner
    data class ReferenceUnit(val substance: SubstanceKey) : SourcePath

    /** The [index]-th assertion of the test that [test] is generated for. */
    data class Assertion(val test: Process, val index: Int) : SourcePath
    data class Parameter(val process: ProcessKey, val name: String) : SourcePath

    /** A process variable, or a for_each variable when [block] is the path of the for_each. */
    data class Variable(val owner: ExchangeOwner, val name: String, val block: BlockPath? = null) : SourcePath

    /** An exchange or a for_each block. */
    data class Exchange(val owner: ExchangeOwner, val block: BlockPath) : SourcePath

    /** A global parameter, global variable or unit. */
    data class Global(val name: String) : SourcePath
    data class DataSource(val name: String) : SourcePath
}

/**
 * Locations of definitions, filled in by the parser. Empty when the expressions were not parsed from a source.
 */
data class SourceMap(private val locations: Map<SourcePath, SourceLocation> = emptyMap()) {
    operator fun get(path: SourcePath): SourceLocation? = locations[path]

    fun paths(): Set<SourcePath> = locations.keys

    operator fun plus(other: SourceMap): SourceMap = SourceMap(locations + other.locations)

    override fun toString(): String = "[sourceMap]"

    companion object {
        fun empty() = SourceMap()
    }
}

/** The key of a process, as used in source paths. */
fun <Q> EProcess<Q>.processKey(): ProcessKey = ProcessKey(name, labels.mapValues { it.value.value })

/** The key of a substance characterization, as used in source paths. */
fun <Q> ESubstanceCharacterization<Q>.substanceKey(): SubstanceKey = with(referenceExchange.substance) {
    SubstanceKey(name, type, compartment, subCompartment)
}

/**
 * The exchange (as an [EBlockEntry]) or for_each block at [path], or null if there is none.
 */
fun <Q> EProcess<Q>.blockAt(path: BlockPath): BlockExpression<*, Q>? {
    val index = path.indices.firstOrNull() ?: return null
    val rest = path.indices.drop(1)
    return when (path.section) {
        Section.PRODUCTS -> if (rest.isEmpty()) products.getOrNull(index)?.let { EBlockEntry(it) } else null
        Section.INPUTS -> blockAt(inputs, index, rest)
        Section.BIOSPHERE -> blockAt(biosphere, index, rest)
        Section.IMPACTS -> blockAt(impacts, index, rest)
    }
}

/**
 * The impact (as an [EBlockEntry]) or for_each block at [path], or null if there is none.
 * Substances only have impacts.
 */
fun <Q> ESubstanceCharacterization<Q>.blockAt(path: BlockPath): BlockExpression<*, Q>? {
    val index = path.indices.firstOrNull() ?: return null
    return if (path.section == Section.IMPACTS) blockAt(impacts, index, path.indices.drop(1)) else null
}

private fun <E, Q> blockAt(blocks: List<BlockExpression<E, Q>>, index: Int, rest: List<Int>): BlockExpression<E, Q>? {
    val block = blocks.getOrNull(index) ?: return null
    if (rest.isEmpty()) return block
    return when (block) {
        is EBlockEntry -> null
        is EBlockForEach -> blockAt(block.body, rest.first(), rest.drop(1))
    }
}
