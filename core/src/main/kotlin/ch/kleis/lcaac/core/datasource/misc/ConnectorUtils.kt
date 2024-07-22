package ch.kleis.lcaac.core.datasource.misc

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations

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
