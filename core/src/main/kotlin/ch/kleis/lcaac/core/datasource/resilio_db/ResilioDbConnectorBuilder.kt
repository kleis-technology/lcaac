package ch.kleis.lcaac.core.datasource.resilio_db

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.datasource.ConnectorBuilder
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.datasource.resilio_db.api.LcStepMapping
import ch.kleis.lcaac.core.datasource.resilio_db.api.SupportedEndpoint

class ResilioDbConnectorBuilder<Q> : ConnectorBuilder<Q> {
    override fun buildOrNull(factory: ConnectorFactory<Q>, config: ConnectorConfig): DataSourceConnector<Q>? {
        if (config.name != ResilioDbConnectorKeys.RDB_CONNECTOR_NAME) {
            return null
        }
        val requiredOptionKeys = ResilioDbConnectorKeys.requiredOptionKeys()
        val actualOptionKeys = requiredOptionKeys
            .mapNotNull { config.options[it] }
            .toSet()
        if (requiredOptionKeys.size > actualOptionKeys.size) {
            val missingOptionsKeys = requiredOptionKeys.minus(actualOptionKeys)
            throw IllegalArgumentException("connector '${ResilioDbConnectorKeys.RDB_CONNECTOR_NAME}': missing required options: $missingOptionsKeys")
        }

        val primaryKey = config.options[ResilioDbConnectorKeys.RDB_PRIMARY_KEY]!!
        val paramsFrom = config.options[ResilioDbConnectorKeys.RDB_PARAMS_FROM]!!
        val foreignKey = config.options[ResilioDbConnectorKeys.RDB_FOREIGN_KEY]!!
        val url = config.options[ResilioDbConnectorKeys.RDB_URL]!!
        val rawEndpoint = config.options[ResilioDbConnectorKeys.RDB_ENDPOINT]
        val endpoint = rawEndpoint!!
            .let { SupportedEndpoint.from(it) }
            ?: throw IllegalArgumentException("connector '${ResilioDbConnectorKeys.RDB_CONNECTOR_NAME}': unsupported " +
                "endpoint: $rawEndpoint")
        val accessToken = config.options[ResilioDbConnectorKeys.RDB_ACCESS_TOKEN]!!
        val lcStepKey = config.options[ResilioDbConnectorKeys.RDB_LC_STEP_KEY]!!
        val manufacturing = config.options[ResilioDbConnectorKeys.RDB_MANUFACTURING]!!
        val transport = config.options[ResilioDbConnectorKeys.RDB_TRANSPORT]!!
        val use = config.options[ResilioDbConnectorKeys.RDB_USE]!!
        val endOfLife = config.options[ResilioDbConnectorKeys.RDB_END_OF_LIFE]!!

        return ResilioDbConnector(
            config = config,
            factory = factory,
            primaryKey = primaryKey,
            paramsFrom = paramsFrom,
            foreignKey = foreignKey,
            url = url,
            endpoint = endpoint,
            accessToken = accessToken,
            lcStepMapping = LcStepMapping(
                key = lcStepKey,
                manufacturing = manufacturing,
                transport = transport,
                use = use,
                endOfLife = endOfLife,
            )
        )
    }
}
