package ch.kleis.lcaac.core.datasource.misc

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EQuantityAdd
import ch.kleis.lcaac.core.lang.expression.EQuantityMul
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.core.prelude.Prelude

fun <Q> applyFilter(
    sourceName: String,
    ops: QuantityOperations<Q>,
    filter: Map<String, DataValue<Q>>,
): (ERecord<Q>) -> Boolean = { record ->
    filter.entries.all {
        val expected = it.value
        if (expected is StringValue) {
            val actual = record.entries[it.key]
                ?.let { with(ToValue(ops)) { it.toValue() } }
                ?: throw IllegalStateException(
                    "${sourceName}: invalid schema: unknown column '${it.key}'"
                )
            actual == expected
        } else throw EvaluatorException("invalid matching condition $it")
    }
}

fun <Q> reduceSumProduct(
    sourceName: String,
    ops: QuantityOperations<Q>,
    sourceOps: DataSourceOperations<Q>,
    records: Sequence<ERecord<Q>>,
    columns: List<String>,
): DataExpression<Q> {
    val reducer = DataExpressionReducer(
        dataRegister = Prelude.units(),
        dataSourceRegister = DataSourceRegister.empty(),
        ops = ops,
        sourceOps = sourceOps,
    )
    return records.map { record ->
        columns.map { column ->
            record.entries[column]
                ?: throw IllegalStateException(
                    "${sourceName}: invalid schema: unknown column '$column'"
                )
        }.reduce { acc, expression ->
            reducer.reduce(EQuantityMul(acc, expression))
        }
    }.reduce { acc, expression ->
        reducer.reduce(EQuantityAdd(acc, expression))
    }
}
