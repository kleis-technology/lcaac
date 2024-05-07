package ch.kleis.lcaac.core.config

data class LcaacConnectorConfig(
    val name: String,
    val options: Map<String, String>,
)
