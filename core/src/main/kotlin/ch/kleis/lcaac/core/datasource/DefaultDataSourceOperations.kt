package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EQuantityAdd
import ch.kleis.lcaac.core.lang.expression.EQuantityMul
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.core.prelude.Prelude

class DefaultDataSourceOperations<Q>(
    private val config: LcaacConfig,
    private val ops: QuantityOperations<Q>,
    private val connectorFactory: ConnectorFactory<Q> = ConnectorFactory(config, ops)
) : DataSourceOperations<Q> {
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
        val reducer = DataExpressionReducer(
            dataRegister = Prelude.units(),
            dataSourceRegister = DataSourceRegister.empty(),
            ops = ops,
            sourceOps = this,
        )
        return getAll(source).map { record ->
            columns.map { column ->
                record.entries[column]
                    ?: throw IllegalStateException(
                        "${source.config.name}: invalid schema: unknown column '$column'"
                    )
            }.reduce { acc, expression ->
                reducer.reduce(EQuantityMul(acc, expression))
            }
        }.reduce { acc, expression ->
            reducer.reduce(EQuantityAdd(acc, expression))
        }
    }
}
