package ch.kleis.lcaac.core.config

import ch.kleis.lcaac.core.datasource.csv.CsvConnectorConfig
import kotlinx.serialization.Serializable

@Serializable
data class LcaacConfig(
    val name: String = "",
    val description: String = "",
    val datasources: List<DataSourceConfig> = emptyList(),
    val connectors: List<ConnectorConfig> = listOf(CsvConnectorConfig.default()),
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

