package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.evaluator.ArgumentFrame
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.source.SourcePath
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import java.lang.Double.parseDouble

fun <Q> dataExpressionMap(
    ops: QuantityOperations<Q>,
    globals: Map<String, String>,
): Map<DataKey, DataExpression<Q>> {
    val mapper = CoreMapper(ops)
    return globals
        .mapKeys { DataKey(it.key) }
        .mapValues {
            mapper.dataExpression(parseDataExpression(it.value, "<global ${it.key.name}>"))
        }
}

fun smartParseQuantityWithDefaultUnit(s: String, defaultUnit: DataExpression<BasicNumber>): DataExpression<BasicNumber> {
    val parts = s.split(" ")
    return if (parts.size == 1) {
        val number = parts[0]
        val amount = try {
            parseDouble(number)
        } catch (e: NumberFormatException) {
            throw EvaluatorException("'$s' is not a valid quantity")
        }
        EQuantityScale(BasicNumber(amount), defaultUnit)
    } else {
        try {
            CoreMapper(BasicOperations).dataExpression(parseDataExpression(s))
        } catch (e: SyntaxException) {
            throw EvaluatorException("'$s' is not a valid quantity")
        } catch (e: IllegalStateException) {
            throw EvaluatorException("'$s' is not a valid quantity")
        }
    }
}

/**
 * The values of [current] given in [overrides], e.g. with -D or in a request file. A given quantity is
 * evaluated now, so that an error in it is reported as an error in the argument, not in the definition
 * that it replaces.
 */
fun parseOverrides(
    dataReducer: DataExpressionReducer<BasicNumber>,
    current: Map<String, DataExpression<BasicNumber>>,
    overrides: Map<String, String>,
): Map<String, DataExpression<BasicNumber>> = current.mapValues { entry ->
    fun given(name: String, text: String, parse: () -> DataExpression<BasicNumber>): DataExpression<BasicNumber> =
        try {
            parse().also { dataReducer.reduce(it) }
        } catch (e: EvaluatorException) {
            throw e.within(ArgumentFrame(SourcePath.Argument(name), text))
        }

    when (val v = entry.value) {
        is QuantityExpression<*> -> overrides[entry.key]?.let {
            given(entry.key, it) { smartParseQuantityWithDefaultUnit(it, EUnitOf(v)) }
        } ?: entry.value

        is StringExpression -> overrides[entry.key]?.let {
            EStringLiteral(it)
        } ?: entry.value

        is EDefaultRecordOf -> {
            val dataSource = dataReducer.evalDataSource(v.dataSource)
            val schema = dataSource.schema
            val entries = schema.mapValues { schemaEntry ->
                when (val defaultValue = schemaEntry.value) {
                    is QuantityValue -> overrides[schemaEntry.key]?.let {
                        given(schemaEntry.key, it) { smartParseQuantityWithDefaultUnit(it, defaultValue.unit.toEUnitLiteral()) }
                    } ?: defaultValue.toEQuantityScale()

                    is StringValue -> overrides[schemaEntry.key]?.let {
                        EStringLiteral(it)
                    } ?: defaultValue.toEStringLiteral()

                    else -> throw EvaluatorException("datasource '${dataSource.config.name}': column '${
                        schemaEntry
                            .key
                    }': invalid default value")
                }
            }
            ERecord(entries)
        }

        else -> throw EvaluatorException("$v is not a supported data expression")
    }
}

fun prepareArguments(
    dataReducer: DataExpressionReducer<BasicNumber>,
    template: EProcessTemplate<BasicNumber>,
    request: Map<String, String>,
) = parseOverrides(dataReducer, template.params, request)
