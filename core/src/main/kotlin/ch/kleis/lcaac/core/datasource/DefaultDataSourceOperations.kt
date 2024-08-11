package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.math.QuantityOperations

class DefaultDataSourceOperations<Q>(
    private val ops: QuantityOperations<Q>,
    private val connectorFactory: ConnectorFactory<Q>,
) : DataSourceOperations<Q> {
    private val config: LcaacConfig = connectorFactory.getLcaacConfig()
    private val connectors = config.connectors
        .mapNotNull { connectorFactory.buildOrNull(it) }
        .associateBy { it.getName() }

    private fun configOf(source: DataSourceValue<Q>): DataSourceConfig {
        return with(DataSourceConfig.merger(source.config.name)) {
            config.getDataSource(source.config.name)
                ?.let { source.config.combine(it) }
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
        return connector.getFirst(sourceConfig, source)
    }

    override fun getAll(source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        val sourceConfig = configOf(source)
        val connector = connectorOf(sourceConfig)
        return connector.getAll(sourceConfig, source)
    }

    override fun sumProduct(source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q> {
        TODO()
    }
}
