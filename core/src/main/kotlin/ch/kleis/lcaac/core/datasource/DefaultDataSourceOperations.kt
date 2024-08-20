package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.cache.SourceOpsCache
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnector
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnectorKeys
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

class DefaultDataSourceOperations<Q>(
    private val ops: QuantityOperations<Q>,
    private val config: LcaacConfig,
    private val connectors: Map<ConnectorName, DataSourceConnector<Q>>,
    private val cache: Map<ConnectorName, SourceOpsCache<Q>> = connectors
        .filter { it.value.getConfig().cache.enabled }
        .mapValues {
            SourceOpsCache(
                it.value.getConfig().cache.maxSize,
                it.value.getConfig().cache.maxRecordsPerCacheLine,
            )
        }
) : DataSourceOperations<Q> {

    fun overrideWith(inMemoryConnector: InMemoryConnector<Q>): DefaultDataSourceOperations<Q> =
        DefaultDataSourceOperations(
            ops,
            inMemoryConnector.getSourceNames()
                .fold(config) { cfg, source ->
                    cfg.setOrModifyDatasource(DataSourceConfig(
                        name = source,
                        connector = InMemoryConnectorKeys.IN_MEMORY_CONNECTOR_NAME,
                    ))
                },
            connectors.plus(inMemoryConnector.getName() to inMemoryConnector),
            if (inMemoryConnector.getConfig().cache.enabled)
                cache.plus(inMemoryConnector.getName() to SourceOpsCache(
                    inMemoryConnector.getConfig().cache.maxSize,
                    inMemoryConnector.getConfig().cache.maxRecordsPerCacheLine,
                ))
            else cache
        )

    private fun configOf(source: DataSourceValue<Q>): DataSourceConfig {
        return with(DataSourceConfig.merger(source.config.name)) {
            config.getDataSource(source.config.name)
                ?.let { source.config.combine(it) } // lcaac config takes precedence
                ?: source.config
        }
    }

    private fun connectorOf(config: DataSourceConfig): DataSourceConnector<Q> {
        return connectors[config.connector]
            ?: throw IllegalArgumentException("Unknown connector '${config.connector}'")
    }

    override fun getFirst(source: DataSourceValue<Q>): ERecord<Q> {
        val sourceConfig = configOf(source)
        val connector = connectorOf(sourceConfig)
        if (connector.getConfig().cache.enabled) {
            val connectorCache = cache[connector.getName()]
                ?: throw IllegalStateException("internal error: cache not found for cache-enabled connector '${connector.getName()}'")
            return connectorCache.recordGetFirst(sourceConfig, source) {
                connector.getFirst(this, sourceConfig, source)
            }
        } else return connector.getFirst(this, sourceConfig, source)
    }

    override fun getAll(source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        val sourceConfig = configOf(source)
        val connector = connectorOf(sourceConfig)
        if (connector.getConfig().cache.enabled) {
            val connectorCache = cache[connector.getName()]
                ?: throw IllegalStateException("internal error: cache not found for cache-enabled connector '${connector.getName()}'")
            return connectorCache.recordGetAll(sourceConfig, source) {
                connector.getAll(this, sourceConfig, source).toList()
            }.asSequence()
        } else return connector.getAll(this, sourceConfig, source)
    }

    override fun sumProduct(source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q> {
        val sourceConfig = configOf(source)
        val connector = connectorOf(sourceConfig)
        if (connector.getConfig().cache.enabled) {
            val connectorCache = cache[connector.getName()]
                ?: throw IllegalStateException("internal error: cache not found for cache-enabled connector '${connector.getName()}'")
            return connectorCache.recordSumProduct(
                sourceConfig,
                source,
                columns,
            ) {
                runSumProduct(source, columns)
            }
        } else return runSumProduct(source, columns)
    }

    private fun runSumProduct(source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q> {
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
        }.fold(zero as DataExpression<Q>, ({ acc, expression ->
            reducer.reduce(EQuantityAdd(acc, expression))
        }))
    }
}
