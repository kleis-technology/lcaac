package ch.kleis.lcaac.core.config

data class DataSourceConfig(
    val name: String,
    val connector: String? = null,
    val location: String? = null,
    val primaryKey: String? = null,
    val options: Map<String, String> = emptyMap(),
)
