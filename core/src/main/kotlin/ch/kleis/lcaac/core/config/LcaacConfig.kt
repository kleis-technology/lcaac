package ch.kleis.lcaac.core.config

import ch.kleis.lcaac.core.datasource.csv.CsvConnectorKeys
import kotlinx.serialization.Serializable

@Serializable
data class LcaacConfig(
    val name: String = "",
    val description: String = "",
    val datasources: List<DataSourceConfig> = emptyList(),
    val connectors: List<ConnectorConfig> = listOf(CsvConnectorKeys.defaultConfig()),
) {
    private val datasourcesMap = datasources.associateBy { it.name }
    private val connectorsMap = connectors.associateBy { it.name }

    fun getDataSource(name: String): DataSourceConfig? {
        return datasourcesMap[name]
    }

    fun getConnector(name: String): ConnectorConfig? {
        return connectorsMap[name]
    }

    fun modifyConnector(name: String, fn: (ConnectorConfig) -> ConnectorConfig): LcaacConfig =
        this.copy(
            connectors = this.connectors.map {
                if (it.name == name) fn(it)
                else it
            }
        )
}

