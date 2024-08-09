package ch.kleis.lcaac.core.datasource.in_memory

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.QuantityOperations

class InMemoryConnector<Q>(
    private val content: Map<String, InMemoryDatasource>,
    private val ops: QuantityOperations<Q>,
) : DataSourceConnector<Q> {
    override fun getName(): String {
        return InMemoryConnectorConfig.IN_MEMORY_CONNECTOR_NAME
    }

    @Suppress("DuplicatedCode")
    override fun sumProduct(config: DataSourceConfig, source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q> {
        val sourceName = config.name
        val schema = source.schema
        val records = content[sourceName]
            ?.records
            ?.filter(applyFilter(source.filter))
            ?: throw EvaluatorException("in_memory: unknown datasource '$sourceName'")
        val amount = records
            .map { record ->
                columns.map { column ->
                    when (val v = record[column]) {
                        is InMemNum -> v.value
                        is InMemStr -> throw EvaluatorException("Expected 'InMemNum' for column '$column', found 'InMemStr'")
                        null -> throw EvaluatorException("Unknown column '$column'")
                    }
                }.reduce(Double::times)
            }.reduce(Double::plus)

        val unit = columns
            .map {
                val defaultValue = schema[it]
                    ?: throw EvaluatorException("Unknown column '$it'")
                when (defaultValue) {
                    is QuantityValue -> defaultValue.unit
                    is RecordValue -> throw EvaluatorException("Expected 'QuantityValue' for column '$it', found 'RecordValue'")
                    is StringValue -> throw EvaluatorException("Expected 'QuantityValue' for column '$it', found 'StringValue'")
                }
            }.reduce { acc, unitValue -> acc.times(unitValue) }
        return EQuantityScale(
            ops.pure(amount),
            unit.toEUnitLiteral(),
        )
    }

    override fun getAll(config: DataSourceConfig, source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        val sourceName = config.name
        val filter = source.filter
        val records = content[sourceName]
            ?.records
            ?.filter(applyFilter(filter))
            ?.map { eRecord(source.schema, it, ops) }
            ?: emptyList()
        return records.asSequence()
    }

    override fun getFirst(config: DataSourceConfig, source: DataSourceValue<Q>): ERecord<Q> {
        return getAll(config, source).firstOrNull()
            ?: throw EvaluatorException("no record found in datasource '${config.name}' matching ${source.filter}")
    }
}

private fun <Q> applyFilter(
    filter: Map<String, DataValue<Q>>,
): (InMemoryRecord) -> Boolean = { record ->
    filter.entries.all {
        val expected = it.value
        if (expected is StringValue) {
            when (val v = record[it.key]) {
                is InMemStr -> expected.s == v.value
                is InMemNum -> throw EvaluatorException("Invalid type for column '${it.key}': expected 'InMemStr', found 'InMemNum'")
                null -> throw EvaluatorException("Unknown column '${it.key}'")
            }
        } else throw EvaluatorException("invalid matching condition $it")
    }
}

private fun <Q> eRecord(schema: Map<String, DataValue<Q>>, record: InMemoryRecord, ops: QuantityOperations<Q>): ERecord<Q> {
    val entries = schema.mapValues {
        when (val defaultValue = it.value) {
            is QuantityValue -> when (val v = record[it.key]) {
                is InMemNum -> EQuantityScale(ops.pure(v.value), defaultValue.unit.toEUnitLiteral())
                is InMemStr -> throw EvaluatorException("Invalid type in column '${it.key}': expected " +
                    "number, found string")

                null -> throw EvaluatorException("Missing column '${it.key}'")
            }

            is StringValue -> when (val v = record[it.key]) {
                is InMemStr -> EStringLiteral(v.value)
                is InMemNum -> throw EvaluatorException("Invalid type in column '${it.key}': expected " +
                    "string, found number")

                null -> throw EvaluatorException("Missing column '${it.key}'")
            }

            is RecordValue -> throw EvaluatorException("Unsupported type: RecordValue")
        }
    }
    return ERecord(entries)
}
