package ch.kleis.lcaac.core.datasource.resilio_db

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.datasource.ConnectorBuilder
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DataSourceConnector

class ResilioDbConnectorBuilder<Q> : ConnectorBuilder<Q> {
    override fun buildOrNull(factory: ConnectorFactory<Q>, config: ConnectorConfig): DataSourceConnector<Q>? {
        return config.resilioDb()
            ?.let { ResilioDbConnector(factory, it) }
    }
}
