package ch.kleis.lcaac.core.datasource.resilio_db

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.datasource.ConnectorBuilder
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DataSourceConnector

class ResilioDbConnectorBuilder<Q> : ConnectorBuilder<Q> {
    override fun buildOrNull(factory: ConnectorFactory<Q>, config: ConnectorConfig): DataSourceConnector<Q>? {
        if (config.name != ResilioDbConnectorKeys.RESILIO_DB_CONNECTOR_NAME) {
            return null
        }
        val accessToken = config.options[ResilioDbConnectorKeys.RESILIO_DB_ACCESS_TOKEN]
            ?: throw IllegalArgumentException("Missing access token in Resilio DB config")
        val url = config.options[ResilioDbConnectorKeys.RESILIO_DB_CONNECTOR_URL]
            ?: "https://db.resilio.tech"
        return ResilioDbConnector(
            config,
            factory,
            url,
            accessToken,
        )
    }
}
