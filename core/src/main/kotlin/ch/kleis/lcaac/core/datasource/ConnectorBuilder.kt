package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.ConnectorConfig

interface ConnectorBuilder<Q> {
    fun buildOrNull(
        factory: ConnectorFactory<Q>,
        config: ConnectorConfig,
    ): DataSourceConnector<Q>?
}
