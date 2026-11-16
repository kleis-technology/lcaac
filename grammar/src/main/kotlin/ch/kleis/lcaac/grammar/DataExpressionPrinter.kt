package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.QuantityOperations
import kotlin.math.abs

/**
 * Prints data expressions in the syntax of the language, with the parentheses that the grammar needs:
 * parsing the result of [print] gives the same expression for an expression written in the language,
 * and an expression with the same value otherwise.
 *
 * [valueOf] can replace a name by its value, e.g. to show a formula with the values it was evaluated
 * with. A record entry `row.mass` is then replaced by the value of the entry. [format] prints numbers.
 * Values (records, composite units) have no syntax and are printed for display only.
 */
class DataExpressionPrinter<Q>(
    private val ops: QuantityOperations<Q>,
    private val format: (Double) -> String = ::formatNumber,
    private val valueOf: (String) -> DataExpression<Q>? = { null },
) {
    // Precedence levels, from the loosest to the tightest, as in the grammar
    private companion object {
        const val OPEN = 0 // lookup, default_record: their data source filter would take what follows
        const val ADD = 1
        const val SCALE = 2 // `2 a * b` is `2 (a * b)`
        const val MUL = 3
        const val POW = 4
        const val ATOM = 5
    }

    fun print(expression: DataExpression<Q>): String = print(expression, OPEN)

    fun print(expression: DataSourceExpression<Q>): String {
        val filter = LinkedHashMap<String, DataExpression<Q>>()
        var current = expression
        while (current is EFilter) {
            // As in the evaluator, an inner filter overrides an outer one on the same column
            current.filter.forEach { (column, value) -> filter[column] = value }
            current = current.dataSource
        }
        val name = when (current) {
            is EDataSourceRef -> current.name
            is EDataSource -> {
                current.filter.forEach { (column, value) -> filter.putIfAbsent(column, value) }
                current.config.name
            }
            is EFilter -> throw IllegalStateException("unreachable")
        }
        val selectors = filter.entries.map { "${it.key} = ${print(it.value, ADD)}" }
        return when (selectors.size) {
            0 -> name
            1 -> "$name match ${selectors.single()}"
            else -> "$name match (${selectors.joinToString(", ")})"
        }
    }

    private fun print(expression: DataExpression<Q>, context: Int): String {
        val (text, precedence) = when (expression) {
            is EQuantityAdd -> "${print(expression.leftHandSide, ADD)} + ${print(expression.rightHandSide, SCALE)}" to ADD
            is EQuantitySub -> "${print(expression.leftHandSide, ADD)} - ${print(expression.rightHandSide, SCALE)}" to ADD
            // A scaled quantity needs no parentheses on either side of `*`, nor on the left of `/`: `2 a * b` reads
            // `2 (a * b)` and `a * 2 b / c` reads `a * (2 (b / c))`, which have the same values. On the right of `/`
            // it does: `a / 2 b * c` would read `a / (2 (b * c))`.
            is EQuantityMul -> "${print(expression.leftHandSide, SCALE)} * ${print(expression.rightHandSide, SCALE)}" to MUL
            is EQuantityDiv -> "${print(expression.leftHandSide, SCALE)} / ${print(expression.rightHandSide, POW)}" to MUL
            is EQuantityScale -> "${number(expression.scale)} ${print(expression.base, SCALE)}" to SCALE
            is EQuantityPow -> "${print(expression.quantity, POW)}^${format(expression.exponent)}" to POW
            is EUnitLiteral -> expression.symbol.toString() to ATOM
            is EUnitAlias -> expression.symbol to ATOM
            is EUnitOf -> "unit_of(${print(expression.expression)})" to ATOM
            is EDataRef -> valueOf(expression.name)
                ?.let { return print(it, context) }
                ?: (expression.name to ATOM)
            is ERecordEntry -> recordEntry(expression)?.let { return print(it, context) }
                ?: ("${print(expression.record, ATOM)}.${expression.index}" to ATOM)
            is EStringLiteral -> "\"${expression.value}\"" to ATOM
            is ERecord -> expression.entries.entries.joinToString(", ", "{", "}") { "${it.key}: ${print(it.value)}" } to ATOM
            is EFirstRecordOf -> "lookup ${print(expression.dataSource)}" to OPEN
            is EDefaultRecordOf -> "default_record from ${print(expression.dataSource)}" to OPEN
            is ESumProduct -> "sum(${print(expression.dataSource)}, ${expression.columns.joinToString(" * ")})" to ATOM
            is EQuantityClosure -> return print(expression.expression, context)
        }
        return if (precedence < context) "($text)" else text
    }

    /** The value of `record.index` when the record is a name that [valueOf] replaces by a record. */
    private fun recordEntry(expression: ERecordEntry<Q>): DataExpression<Q>? {
        val record = (expression.record as? EDataRef)?.let { valueOf(it.name) } as? ERecord ?: return null
        return record.entries[expression.index]
    }

    private fun number(value: Q): String = format(with(ops) { value.toDouble() })
}

/** A number as the grammar reads it, without a useless decimal part: `2`, `0.5`, `1.0E-9`. */
fun formatNumber(value: Double): String =
    if (value == Math.rint(value) && abs(value) < 1e15) value.toLong().toString() else value.toString()
