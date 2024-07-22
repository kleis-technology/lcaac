package ch.kleis.lcaac.core.datasource.in_memory

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.datasource.ConnectorBuilder
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.lang.expression.ERecord

class InMemoryConnectorBuilder<Q>(
    private val content: Map<String, List<ERecord<Q>>>,
) : ConnectorBuilder<Q> {
    override fun buildOrNull(factory: ConnectorFactory<Q>, config: ConnectorConfig): DataSourceConnector<Q>? {
        return config
            .inMemory(content)?.let { InMemoryConnector(it, factory.getQuantityOperations()) }
    }
}
