package ch.kleis.lcaac.core.config

data class LcaacConfig(
    val name: String,
    val description: String,
    val datasources: Map<String, DataSourceConfig>,
    val connectors: Map<String, ConnectorConfig>,
)

