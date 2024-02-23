package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EQuantityAdd
import ch.kleis.lcaac.core.lang.expression.EQuantityMul
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.core.prelude.Prelude

open class DataSourceOperationsBase<Q>(
    private val ops: QuantityOperations<Q>,
    private val load: (DataSourceDescription<Q>) -> Sequence<ERecord<Q>>,
) : DataSourceOperations<Q> {
    override fun readAll(source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        val description = DataSourceDescription(source.location, source.schema)
        val records = load(description)
        val filter = source.filter
        val reducer = DataExpressionReducer(
            dataRegister = Prelude.units(),
            dataSourceRegister = DataSourceRegister.empty(),
            ops = ops,
            sourceOps = this,
        )
        return records
            .filter { record ->
                filter.entries.all {
                    val expected = it.value
                    if (expected is StringValue) {
                        val actual = record.entries[it.key]
                            ?.let { with(ToValue(ops)) { it.toValue() } }
                            ?: throw IllegalStateException(
                                "${source.location}: invalid schema: unknown column '${it.key}'"
                            )
                        actual == expected
                    } else throw EvaluatorException("invalid matching condition $it")
                }
            }.map { record ->
                ERecord(
                    record.entries.mapValues { reducer.reduce(it.value) }
                )
            }
    }

    override fun sumProduct(source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q> {
        val reducer = DataExpressionReducer(
            dataRegister = Prelude.units(),
            dataSourceRegister = DataSourceRegister.empty(),
            ops = ops,
            sourceOps = this,
        )
        return readAll(source).map { record ->
            columns.map { column ->
                record.entries[column]
                    ?: throw IllegalStateException(
                        "${source.location}: invalid schema: unknown column '$column'"
                    )
            }.reduce { acc, expression ->
                reducer.reduce(EQuantityMul(acc, expression))
            }
        }.reduce { acc, expression ->
            reducer.reduce(EQuantityAdd(acc, expression))
        }
    }

    override fun getFirst(source: DataSourceValue<Q>): ERecord<Q> {
        return readAll(source).firstOrNull()
            ?: throw EvaluatorException("no record found in '${source.location}' matching ${source.filter}")
    }
}
