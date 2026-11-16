package ch.kleis.lcaac.core.lang.evaluator.derivation

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.source.SourcePath
import ch.kleis.lcaac.core.lang.source.blockAt
import ch.kleis.lcaac.core.lang.value.DataSourceValue

/** A row of a for_each block: the block, and the index of the row in the data source. */
data class RowRef(val forEach: SourcePath.Exchange, val row: Int)

/**
 * Where an evaluated exchange comes from: the exchange as written, and the for_each rows
 * that produced it, from the outermost to the innermost.
 */
data class ExchangeOrigin(
    val path: SourcePath.Exchange,
    val rows: List<RowRef> = emptyList(),
)

/**
 * The origin of each exchange of an evaluated process, in the same order as the exchanges
 * of the evaluated process (and of its [ch.kleis.lcaac.core.lang.value.ProcessValue]).
 */
data class ProcessOrigins(
    val products: List<ExchangeOrigin> = emptyList(),
    val inputs: List<ExchangeOrigin> = emptyList(),
    val biosphere: List<ExchangeOrigin> = emptyList(),
    val impacts: List<ExchangeOrigin> = emptyList(),
)

enum class ReadKind { LOOKUP, SUM, DEFAULT_RECORD, FOR_EACH }

/** A read of a data source: the source with its filter, and the records it returned. */
data class DataSourceRead<Q>(
    val kind: ReadKind,
    val dataSource: DataSourceValue<Q>,
    val records: List<ERecord<Q>>,
    /** The columns multiplied and summed, for [ReadKind.SUM]. */
    val columns: List<String> = emptyList(),
)

/**
 * The value of a parameter: [expression] is the argument actually used, given by the caller
 * or the [default] value. [value] is null if the argument could not be evaluated, which only
 * matters if the process does not use it.
 */
data class ParameterDerivation<Q>(
    val path: SourcePath.Parameter,
    val expression: DataExpression<Q>,
    val default: DataExpression<Q>?,
    val value: DataExpression<Q>?,
)

/**
 * The value of a variable (of the process, of a for_each row, or global), with the data source
 * reads made directly by its expression. A for_each variable has one derivation per row.
 */
data class VariableDerivation<Q>(
    val path: SourcePath,
    val rows: List<RowRef>,
    val expression: DataExpression<Q>,
    val value: DataExpression<Q>,
    val reads: List<DataSourceRead<Q>>,
)

/** The rows read by a for_each block (one derivation per enclosing row). */
data class ForEachDerivation<Q>(
    val path: SourcePath.Exchange,
    val rows: List<RowRef>,
    val rowRef: String,
    val read: DataSourceRead<Q>,
)

/**
 * How a process was evaluated: its parameters, variables and data source reads, and where
 * each of its exchanges comes from. Recorded when the evaluator is asked to.
 */
data class ProcessDerivation<Q>(
    val process: SourcePath.Process,
    val template: EProcessTemplate<Q>,
    val origins: ProcessOrigins,
    val parameters: List<ParameterDerivation<Q>>,
    val variables: List<VariableDerivation<Q>>,
    val forEach: List<ForEachDerivation<Q>>,
    /** Data source reads made directly by the quantity of an exchange. */
    val exchangeReads: Map<ExchangeOrigin, List<DataSourceRead<Q>>>,
) {
    /** The exchange as written in the process, before parameters are replaced by their values. */
    fun expressionOf(origin: ExchangeOrigin): LcaExchangeExpression<Q>? =
        (template.body.blockAt(origin.path.block) as? EBlockEntry<*, Q>)?.entry as? LcaExchangeExpression<Q>

    private val variablesByKey by lazy { variables.associateBy { it.path to it.rows } }

    /**
     * The value of [name] where the evaluation was in the for_each [rows], with the rules of the evaluator:
     * the row and variables of each enclosing for_each, from the innermost, then the variables, parameters and
     * labels of the process, then the globals. Null for a unit, or for a name that the evaluation did not use.
     */
    fun valueOf(name: String, rows: List<RowRef> = emptyList()): DataExpression<Q>? {
        for (i in rows.indices.reversed()) {
            val row = rows[i]
            val block = template.body.blockAt(row.forEach.block) as? EBlockForEach<*, Q> ?: continue
            if (name == block.rowRef) {
                val enclosing = rows.subList(0, i)
                return forEach.firstOrNull { it.path == row.forEach && it.rows == enclosing }
                    ?.read?.records?.getOrNull(row.row)
            }
            if (block.locals.containsKey(name)) {
                return variablesByKey[SourcePath.Variable(process, name, row.forEach.block) to rows.subList(0, i + 1)]?.value
            }
        }
        if (template.locals.containsKey(name)) return variablesByKey[SourcePath.Variable(process, name) to emptyList()]?.value
        parameters.firstOrNull { it.path.name == name }?.let { return it.value }
        template.body.labels[name]?.let { return it }
        return variablesByKey[SourcePath.Global(name) to emptyList()]?.value
    }
}
