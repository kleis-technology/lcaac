package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnector
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.RecordValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.core.prelude.Prelude

typealias ConnectorName = String
typealias SourceName = String

class DefaultDataSourceOperations<Q>(
    private val ops: QuantityOperations<Q>,
    private val config: LcaacConfig,
    private val connectors: Map<ConnectorName, DataSourceConnector<Q>>,
    private val overrides: Map<SourceName, ConnectorName>,
) : DataSourceOperations<Q> {

    fun overrideWith(inMemoryConnector: InMemoryConnector<Q>): DefaultDataSourceOperations<Q> =
        DefaultDataSourceOperations(
            ops,
            config,
            connectors.plus(inMemoryConnector.getName() to inMemoryConnector),
            overrides.plus(inMemoryConnector.getSourceNames().map { it to inMemoryConnector.getName() }),
        )

    private fun configOf(source: DataSourceValue<Q>): DataSourceConfig {
        return with(DataSourceConfig.merger(source.config.name)) {
            config.getDataSource(source.config.name)
                ?.let { source.config.combine(it) }
                ?: source.config
        }
    }

    private fun connectorOf(config: DataSourceConfig): DataSourceConnector<Q> {
        return overrides[config.name]
            ?.let { connectors[it] }
            ?: connectors[config.connector]
            ?: throw IllegalArgumentException("Unknown connector '${config.connector}'")
    }

    override fun getFirst(source: DataSourceValue<Q>): ERecord<Q> {
        val sourceConfig = configOf(source)
        val connector = connectorOf(sourceConfig)
        return connector.getFirst(sourceConfig, source)
    }

    override fun getAll(source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        val sourceConfig = configOf(source)
        val connector = connectorOf(sourceConfig)
        return connector.getAll(sourceConfig, source)
    }

    override fun sumProduct(source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q> {
        val reducer = DataExpressionReducer(
            dataRegister = Prelude.units(),
            dataSourceRegister = DataSourceRegister.empty(),
            ops = ops,
            sourceOps = this,
        )
        val sourceName = source.config.name
        val schema = source.schema
        if (columns.isEmpty()) {
            throw IllegalArgumentException("${sourceName}: cannot perform sum product: empty list of columns")
        }
        val zero = columns.map { column ->
            when (val defaultValue = schema[column]) {
                is QuantityValue -> defaultValue.unit.toEUnitLiteral()
                is RecordValue -> throw IllegalStateException(
                    "${sourceName}: cannot perform sum product: column '$column': expected number, found record"
                )

                is StringValue -> throw IllegalStateException(
                    "${sourceName}: cannot perform sum product: column '$column': expected number, found string"
                )

                null -> throw IllegalStateException(
                    "${sourceName}: schema: unknown column '$column'"
                )
            }
        }.reduce { acc, unitValue -> acc.times(unitValue) }
            .let { EQuantityScale(ops.pure(0.0), it) }
        return getAll(source).map { record ->
            columns.map { column ->
                record.entries[column]
                    ?: throw IllegalStateException(
                        "${sourceName}: invalid schema: unknown column '$column'"
                    )
            }.reduce { acc, expression ->
                reducer.reduce(EQuantityMul(acc, expression))
            }
        }.fold(zero as DataExpression<Q>, ({ acc, expression -> reducer.reduce(EQuantityAdd(acc, expression)) }))
    }
}
