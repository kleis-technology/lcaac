package ch.kleis.lcaac.core.datasource.csv

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.datasource.ConnectorBuilder
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DataSourceConnector

class CsvConnectorBuilder<Q> : ConnectorBuilder<Q> {
    override fun buildOrNull(factory: ConnectorFactory<Q>, config: ConnectorConfig): DataSourceConnector<Q>? {
        return config
            .csv()?.let { CsvConnector(it, factory.getQuantityOperations()) }
    }
}
