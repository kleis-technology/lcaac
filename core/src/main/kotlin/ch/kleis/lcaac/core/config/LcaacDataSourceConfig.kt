package ch.kleis.lcaac.core.config

data class LcaacDataSourceConfig(
    val name: String,
    val connector: String = "csv",
    val location: String = "$name.csv",
    val primaryKey: String = "id",
    val options: Map<String, String> = emptyMap(),
)
