package ch.kleis.lcaac.core.datasource.resilio_db

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.datasource.resilio_db.api.LcStepMapping
import ch.kleis.lcaac.core.datasource.resilio_db.api.RdbClient
import ch.kleis.lcaac.core.datasource.resilio_db.api.SupportedEndpoint
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue

class ResilioDbConnector<Q>(
    private val config: ConnectorConfig,
    private val factory: ConnectorFactory<Q>,
    primaryKey: String,
    paramsFrom: String,
    foreignKey: String,
    url: String,
    endpoint: SupportedEndpoint,
    accessToken: String,
    lcStepMapping: LcStepMapping,
    private val rdbClient: RdbClient<Q> = RdbClient(
        url = url,
        accessToken = accessToken,
        primaryKey = primaryKey,
        lcStepMapping = lcStepMapping,
        ops = factory.getQuantityOperations(),
    )
) : DataSourceConnector<Q> {
    override fun getName(): String {
        return ResilioDbConnectorKeys.RDB_CONNECTOR_NAME
    }

    override fun getConfig(): ConnectorConfig {
        return config
    }

    override fun getFirst(config: DataSourceConfig, source: DataSourceValue<Q>): ERecord<Q> {
        TODO()
    }

    override fun getAll(config: DataSourceConfig, source: DataSourceValue<Q>): Sequence<ERecord<Q>> {
        TODO("Not yet implemented")
    }
}
