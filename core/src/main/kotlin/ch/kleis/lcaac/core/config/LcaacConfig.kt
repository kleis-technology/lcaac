package ch.kleis.lcaac.core.config

import kotlinx.serialization.Serializable

@Serializable
data class LcaacConfig(
    val name: String = "",
    val description: String = "",
    val datasources: List<DataSourceConfig> = emptyList(),
    val connectors: List<ConnectorConfig> = emptyList(),
) {
    private val datasourcesMap = datasources.associateBy { it.name }
    private val connectorsMap = connectors.associateBy { it.name }

    fun getDataSource(name: String): DataSourceConfig? {
        return datasourcesMap[name]
    }

    fun getConnector(name: String): ConnectorConfig? {
        return connectorsMap[name]
    }
}

