package ch.kleis.lcaac.core.config

import ch.kleis.lcaac.core.datasource.csv.CsvConnectorKeys
import kotlinx.serialization.Serializable

@Serializable
data class LcaacConfig(
    val name: String = "",
    val description: String = "",
    // TODO clean datasources
    val datasources: List<DataSourceConfig> = emptyList(),
    val connectors: List<ConnectorConfig> = listOf(CsvConnectorKeys.defaultConfig()),
) {
    private val datasourcesMap = datasources.associateBy { it.name }

    fun getDataSource(name: String): DataSourceConfig? {
        return datasourcesMap[name]
    }

    fun setOrModifyDatasource(datasource: DataSourceConfig): LcaacConfig {
        val mergedConfig = this.datasources
            .firstOrNull { it.name == datasource.name }
            ?.let { with(DataSourceConfig.merger(it.name)) {
                it.combine(datasource)
            } }
            ?: datasource
        val newDatasources = this.datasources
            .associateBy { it.name }
            .plus(mergedConfig.name to mergedConfig)
            .values.toList()
        return this.copy(
            datasources = newDatasources
        )
    }
}

