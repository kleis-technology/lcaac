package ch.kleis.lcaac.core.datasource.resilio_db

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.datasource.ConnectorBuilder
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DataSourceConnector

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

        val url = config.options[ResilioDbConnectorKeys.RDB_URL]
            ?: throw IllegalArgumentException("connector '${ResilioDbConnectorKeys.RDB_CONNECTOR_NAME}': missing option '${ResilioDbConnectorKeys.RDB_URL}'")
        val accessToken = config.options[ResilioDbConnectorKeys.RDB_ACCESS_TOKEN]
            ?: throw IllegalArgumentException("connector '${ResilioDbConnectorKeys.RDB_CONNECTOR_NAME}': missing option '${ResilioDbConnectorKeys.RDB_ACCESS_TOKEN}'")
        val version = config.options[ResilioDbConnectorKeys.RDB_VERSION]
            ?: throw IllegalArgumentException("connector '${ResilioDbConnectorKeys.RDB_CONNECTOR_NAME}': missing option '${ResilioDbConnectorKeys.RDB_VERSION}'")

        return ResilioDbConnector(
            config = config,
            symbolTable = factory.getSymbolTable(),
            ops = factory.getQuantityOperations(),
            url = url,
            accessToken = accessToken,
            version = version,
        )
    }
}
