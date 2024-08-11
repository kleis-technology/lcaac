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
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import kotlinx.coroutines.runBlocking

typealias ConnectorName = String
typealias SourceName = String

data class GetAllRequest<Q>(
    val config: DataSourceConfig,
    val source: DataSourceValue<Q>
)

data class GetFirstRequest<Q>(
    val config: DataSourceConfig,
    val source: DataSourceValue<Q>
)

data class SumProductRequest<Q>(
    val config: DataSourceConfig,
    val source: DataSourceValue<Q>,
    val columns: List<String>,
)


class ConnectorCache<Q>(
    private val maxSize: Long
) {
    val getAllCache = InMemoryKache<GetAllRequest<Q>, Sequence<ERecord<Q>>>(
        maxSize = maxSize
    ) {
        strategy = KacheStrategy.LRU
    }

    val getFirstCache = InMemoryKache<GetFirstRequest<Q>, ERecord<Q>>(
        maxSize = maxSize,
    ) {
        strategy = KacheStrategy.LRU
    }
    val sumProductCache = InMemoryKache<SumProductRequest<Q>, DataExpression<Q>>(
        maxSize = maxSize,
    ) {
        strategy = KacheStrategy.LRU
    }
}

class DefaultDataSourceOperations<Q>(
    private val ops: QuantityOperations<Q>,
    private val config: LcaacConfig,
    private val connectors: Map<ConnectorName, DataSourceConnector<Q>>,
    private val overrides: Map<SourceName, ConnectorName>,
    private val cache: Map<String, ConnectorCache<Q>> = connectors
        .filter { it.value.getConfig().cache.enabled }
        .mapValues {
            ConnectorCache(it.value.getConfig().cache.maxSize)
        }
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
        if (connector.getConfig().cache.enabled) {
            val connectorCache = cache[connector.getName()]
                ?: throw IllegalStateException("internal error: cache not found for cache-enabled connector '${connector.getName()}'")
            val result = runBlocking {
                connectorCache.getFirstCache.getOrPut(
                    GetFirstRequest(sourceConfig, source)
                ) {
                    connector.getFirst(sourceConfig, source)
                }
            }
            return result
                ?: throw IllegalArgumentException("cannot fetch records from datasource $config")
        } else return connector.getFirst(sourceConfig, source)
    }

    override fun getAll(source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        val sourceConfig = configOf(source)
        val connector = connectorOf(sourceConfig)
        if (connector.getConfig().cache.enabled) {
            val connectorCache = cache[connector.getName()]
                ?: throw IllegalStateException("internal error: cache not found for cache-enabled connector '${connector.getName()}'")
            val result = runBlocking {
                connectorCache.getAllCache.getOrPut(
                    GetAllRequest(sourceConfig, source)
                ) {
                    connector.getAll(sourceConfig, source)
                }
            }
            return result
                ?: throw IllegalArgumentException("cannot fetch records from datasource $config")
        } else return connector.getAll(sourceConfig, source)
    }

    override fun sumProduct(source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q> {
        val sourceConfig = configOf(source)
        val connector = connectorOf(sourceConfig)
        if (connector.getConfig().cache.enabled) {
            val connectorCache = cache[connector.getName()]
                ?: throw IllegalStateException("internal error: cache not found for cache-enabled connector '${connector.getName()}'")
            val result = runBlocking {
                connectorCache.sumProductCache.getOrPut(
                    SumProductRequest(sourceConfig, source, columns)
                ) {
                    rawSumProduct(source, columns)
                }
            }
            return result
                ?: throw IllegalArgumentException("cannot fetch records from datasource $config")
        } else return rawSumProduct(source, columns)
    }

    private fun rawSumProduct(source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q> {
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
