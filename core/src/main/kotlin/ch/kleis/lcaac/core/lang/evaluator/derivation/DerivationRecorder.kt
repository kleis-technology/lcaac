package ch.kleis.lcaac.core.lang.evaluator.derivation

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.lang.source.Section
import ch.kleis.lcaac.core.lang.source.SourcePath

/**
 * Collects, while a process is reduced, where each of its exchanges comes from and,
 * if [recordsValues], how its parameters and variables were evaluated.
 *
 * The reducers call it at the same boundaries where they locate errors. One recorder
 * is used for one process, by one thread.
 */
class DerivationRecorder<Q>(val recordsValues: Boolean = false) {
    private val rows = ArrayList<RowRef>()
    private val readCollectors = ArrayList<MutableList<DataSourceRead<Q>>>()

    private val products = ArrayList<ExchangeOrigin>()
    private val inputs = ArrayList<ExchangeOrigin>()
    private val biosphere = ArrayList<ExchangeOrigin>()
    private val impacts = ArrayList<ExchangeOrigin>()

    private val parameters = ArrayList<ParameterDerivation<Q>>()
    private val variables = LinkedHashMap<Pair<SourcePath, List<RowRef>>, VariableDerivation<Q>>()
    private val forEach = ArrayList<ForEachDerivation<Q>>()
    private val exchangeReads = LinkedHashMap<ExchangeOrigin, List<DataSourceRead<Q>>>()

    /** Reduces an exchange written at [path]; its origin is added once it is reduced. */
    fun <T> exchange(path: SourcePath.Exchange, reduce: () -> T): T {
        val origin = ExchangeOrigin(path, if (rows.isEmpty()) emptyList() else rows.toList())
        val (result, reads) = if (recordsValues) collectReads(reduce) else reduce() to emptyList()
        when (path.block.section) {
            Section.PRODUCTS -> products
            Section.INPUTS -> inputs
            Section.BIOSPHERE -> biosphere
            Section.IMPACTS -> impacts
        }.add(origin)
        if (reads.isNotEmpty()) exchangeReads[origin] = reads
        return result
    }

    /** Reduces the content of a row of the for_each written at [path]. */
    fun <T> row(path: SourcePath.Exchange, row: Int, reduce: () -> T): T {
        rows.add(RowRef(path, row))
        try {
            return reduce()
        } finally {
            rows.removeAt(rows.lastIndex)
        }
    }

    fun forEach(path: SourcePath.Exchange, rowRef: String, read: DataSourceRead<Q>) {
        if (recordsValues) forEach.add(ForEachDerivation(path, rows.toList(), rowRef, read))
    }

    fun parameter(path: SourcePath.Parameter, expression: DataExpression<Q>, default: DataExpression<Q>?, value: DataExpression<Q>?) {
        if (recordsValues) parameters.add(ParameterDerivation(path, expression, default, value))
    }

    /**
     * Reduces the variable at [path]. The first value is kept: the same variable gives
     * the same value in the same rows.
     */
    fun variable(path: SourcePath, expression: DataExpression<Q>, reduce: () -> DataExpression<Q>): DataExpression<Q> {
        if (!recordsValues) return reduce()
        val (value, reads) = collectReads(reduce)
        val variableRows = rowsOf(path)
        variables.putIfAbsent(path to variableRows, VariableDerivation(path, variableRows, expression, value, reads))
        return value
    }

    /** Whether the variable at [path] is already recorded, in the current rows. */
    fun hasVariable(path: SourcePath): Boolean = variables.containsKey(path to rowsOf(path))

    /** Adds a data source read to the variable or exchange being reduced. */
    fun read(read: DataSourceRead<Q>) {
        if (recordsValues) readCollectors.lastOrNull()?.add(read)
    }

    fun origins(): ProcessOrigins = ProcessOrigins(products.toList(), inputs.toList(), biosphere.toList(), impacts.toList())

    fun derivation(process: SourcePath.Process, template: EProcessTemplate<Q>): ProcessDerivation<Q>? =
        if (!recordsValues) null
        else ProcessDerivation(
            process = process,
            template = template,
            origins = origins(),
            parameters = parameters.toList(),
            variables = variables.values.toList(),
            forEach = forEach.toList(),
            exchangeReads = LinkedHashMap(exchangeReads),
        )

    private fun <T> collectReads(reduce: () -> T): Pair<T, List<DataSourceRead<Q>>> {
        val reads = ArrayList<DataSourceRead<Q>>()
        readCollectors.add(reads)
        try {
            return reduce() to reads
        } finally {
            readCollectors.removeAt(readCollectors.lastIndex)
        }
    }

    /**
     * The rows in which a variable is defined: those up to its for_each for a for_each variable,
     * none for a process variable, a parameter or a global, wherever they are used.
     */
    private fun rowsOf(path: SourcePath): List<RowRef> {
        val block = (path as? SourcePath.Variable)?.block ?: return emptyList()
        val index = rows.indexOfLast { it.forEach.block == block && it.forEach.owner == path.owner }
        return if (index < 0) emptyList() else rows.subList(0, index + 1).toList()
    }
}
