package ch.kleis.lcaac.core.config

import kotlinx.serialization.Serializable

@Serializable
data class ConnectorConfig(
    val name: String,
    val options: Map<String, String>,
)
