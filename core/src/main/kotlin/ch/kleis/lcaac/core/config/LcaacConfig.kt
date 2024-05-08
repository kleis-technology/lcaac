package ch.kleis.lcaac.core.config

import kotlinx.serialization.Serializable

@Serializable
data class LcaacConfig(
    val name: String,
    val description: String,
    val datasources: Map<String, DataSourceConfig>,
    val connectors: Map<String, ConnectorConfig>,
)

