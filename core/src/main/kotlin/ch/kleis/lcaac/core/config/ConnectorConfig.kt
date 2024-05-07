package ch.kleis.lcaac.core.config

data class ConnectorConfig(
    val name: String,
    val options: Map<String, String>,
)
