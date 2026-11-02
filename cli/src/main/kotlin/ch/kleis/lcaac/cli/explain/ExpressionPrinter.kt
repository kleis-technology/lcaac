package ch.kleis.lcaac.cli.explain

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import java.math.BigDecimal
import java.math.MathContext
import java.util.*
import kotlin.math.abs

class NumberFormat(private val precision: Int = 6) {
    fun format(d: Double): String {
        if (d == 0.0) return "0"
        if (d.isNaN() || d.isInfinite()) return d.toString()
        val a = abs(d)
        // Whole numbers are printed exactly: rounding 34359738368 to 34359700000 would hide what is being computed
        if (a < 1e15 && d == Math.rint(d)) return BigDecimal(d).toPlainString()
        return if (a >= 1e-4 && a < 1e12) {
            BigDecimal(d).round(MathContext(precision)).stripTrailingZeros().toPlainString()
        } else {
            val s = String.format(Locale.ROOT, "%.${precision - 1}e", d)
            val (mantissa, exponent) = s.split("e")
            val trimmed = if (mantissa.contains('.')) mantissa.trimEnd('0').trimEnd('.') else mantissa
            "${trimmed}e${exponent.toInt()}"
        }
    }

    fun quantity(amount: Double, unit: UnitValue<BasicNumber>): String =
        "${format(amount)} ${unit.symbol}".trim()
}

/**
 * Prints data expressions using the LCA language syntax.
 *
 * [substitute] can replace a sub-expression (typically a reference) by a text,
 * e.g. its value. It returns null to keep the sub-expression as is.
 */
class ExpressionPrinter(
    private val fmt: NumberFormat,
    private val substitute: (DataExpression<BasicNumber>) -> String? = { null },
) {
    private companion object {
        const val ADD = 1
        const val MUL = 2
        const val SCALE = 3
        const val POW = 4
        const val ATOM = 5
    }

    fun print(expression: DataExpression<BasicNumber>): String = print(expression, 0)

    fun printDataSource(expression: DataSourceExpression<BasicNumber>): String {
        val filters = LinkedHashMap<String, DataExpression<BasicNumber>>()
        var current = expression
        while (current is EFilter) {
            filters.putAll(current.filter)
            current = current.dataSource
        }
        val name = when (current) {
            is EDataSourceRef -> current.name
            is EDataSource -> {
                filters.putAll(current.filter)
                current.config.name
            }
            is EFilter -> throw IllegalStateException()
        }
        if (filters.isEmpty()) return name
        val selectors = filters.entries.map { "${it.key} = ${print(it.value, ADD)}" }
        return if (selectors.size == 1) "$name match ${selectors.first()}"
        else "$name match (${selectors.joinToString(", ")})"
    }

    private fun print(expression: DataExpression<BasicNumber>, context: Int): String {
        substitute(expression)?.let { text ->
            val precedence = if (text.contains(' ') && !text.startsWith("{") && !text.startsWith("\"")) SCALE else ATOM
            return wrap(text, precedence, context)
        }
        val (text, precedence) = when (expression) {
            is EQuantityAdd -> "${print(expression.leftHandSide, ADD)} + ${print(expression.rightHandSide, MUL)}" to ADD
            is EQuantitySub -> "${print(expression.leftHandSide, ADD)} - ${print(expression.rightHandSide, MUL)}" to ADD
            is EQuantityMul -> "${print(expression.leftHandSide, MUL)} * ${print(expression.rightHandSide, SCALE)}" to MUL
            is EQuantityDiv -> "${print(expression.leftHandSide, MUL)} / ${print(expression.rightHandSide, POW)}" to MUL
            // `10 percent * x` is parsed as 10 * (percent * x): print it as written
            is EQuantityScale -> when (expression.base) {
                is EQuantityMul, is EQuantityDiv -> "${fmt.format(expression.scale.value)} ${print(expression.base, MUL)}" to MUL
                else -> "${fmt.format(expression.scale.value)} ${print(expression.base, POW)}" to SCALE
            }
            is EQuantityPow -> "${print(expression.quantity, ATOM)}^${fmt.format(expression.exponent)}" to POW
            is EUnitLiteral -> expression.symbol.toString() to ATOM
            is EUnitAlias -> expression.symbol to ATOM
            is EUnitOf -> "unit_of(${print(expression.expression, 0)})" to ATOM
            is EDataRef -> expression.name to ATOM
            is ERecordEntry -> "${print(expression.record, ATOM)}.${expression.index}" to ATOM
            is EStringLiteral -> "\"${expression.value}\"" to ATOM
            is ERecord -> expression.entries.entries
                .joinToString(", ", "{", "}") { "${it.key}: ${print(it.value, 0)}" } to ATOM
            is EFirstRecordOf -> "lookup ${printDataSource(expression.dataSource)}" to 0
            is EDefaultRecordOf -> "default_record from ${printDataSource(expression.dataSource)}" to 0
            is ESumProduct -> "sum(${printDataSource(expression.dataSource)}, ${expression.columns.joinToString(" * ")})" to ATOM
            is EQuantityClosure -> return print(expression.expression, context)
        }
        return wrap(text, precedence, context)
    }

    private fun wrap(text: String, precedence: Int, context: Int): String =
        if (precedence < context) "($text)" else text
}

/** All references used by an expression, including those in data source filters. */
fun referencesOf(expression: DataExpression<BasicNumber>): Set<String> = when (expression) {
    is EDataRef -> setOf(expression.name)
    is EQuantityAdd -> referencesOf(expression.leftHandSide) + referencesOf(expression.rightHandSide)
    is EQuantitySub -> referencesOf(expression.leftHandSide) + referencesOf(expression.rightHandSide)
    is EQuantityMul -> referencesOf(expression.leftHandSide) + referencesOf(expression.rightHandSide)
    is EQuantityDiv -> referencesOf(expression.leftHandSide) + referencesOf(expression.rightHandSide)
    is EQuantityScale -> referencesOf(expression.base)
    is EQuantityPow -> referencesOf(expression.quantity)
    is EUnitOf -> referencesOf(expression.expression)
    is ERecordEntry -> referencesOf(expression.record)
    is ERecord -> expression.entries.values.flatMap { referencesOf(it) }.toSet()
    is EFirstRecordOf -> referencesOf(expression.dataSource)
    is EDefaultRecordOf -> referencesOf(expression.dataSource)
    is ESumProduct -> referencesOf(expression.dataSource)
    is EUnitLiteral, is EUnitAlias, is EStringLiteral, is EQuantityClosure -> emptySet()
}

fun referencesOf(expression: DataSourceExpression<BasicNumber>): Set<String> = when (expression) {
    is EDataSourceRef -> emptySet()
    is EDataSource -> expression.filter.values.flatMap { referencesOf(it) }.toSet()
    is EFilter -> referencesOf(expression.dataSource) + expression.filter.values.flatMap { referencesOf(it) }
}

/** Data source primitives (lookup, default_record, sum) used by an expression, in reading order. */
fun dataSourcePrimitivesOf(expression: DataExpression<BasicNumber>): List<DataExpression<BasicNumber>> = when (expression) {
    is EFirstRecordOf, is EDefaultRecordOf, is ESumProduct -> listOf(expression)
    is EQuantityAdd -> dataSourcePrimitivesOf(expression.leftHandSide) + dataSourcePrimitivesOf(expression.rightHandSide)
    is EQuantitySub -> dataSourcePrimitivesOf(expression.leftHandSide) + dataSourcePrimitivesOf(expression.rightHandSide)
    is EQuantityMul -> dataSourcePrimitivesOf(expression.leftHandSide) + dataSourcePrimitivesOf(expression.rightHandSide)
    is EQuantityDiv -> dataSourcePrimitivesOf(expression.leftHandSide) + dataSourcePrimitivesOf(expression.rightHandSide)
    is EQuantityScale -> dataSourcePrimitivesOf(expression.base)
    is EQuantityPow -> dataSourcePrimitivesOf(expression.quantity)
    is EUnitOf -> dataSourcePrimitivesOf(expression.expression)
    is ERecordEntry -> dataSourcePrimitivesOf(expression.record)
    is ERecord -> expression.entries.values.flatMap { dataSourcePrimitivesOf(it) }
    is EDataRef, is EUnitLiteral, is EUnitAlias, is EStringLiteral, is EQuantityClosure -> emptyList()
}
