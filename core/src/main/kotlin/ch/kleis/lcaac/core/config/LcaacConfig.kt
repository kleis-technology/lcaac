package ch.kleis.lcaac.core.config

import ch.kleis.lcaac.core.datasource.csv.CsvConnectorKeys
import kotlinx.serialization.Serializable

@Serializable
data class LcaacConfig(
    val name: String = "",
    val description: String = "",
    val connectors: List<ConnectorConfig> = listOf(CsvConnectorKeys.defaultConfig()),
)

