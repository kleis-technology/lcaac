package ch.kleis.lcaac.core.datasource.csv

import ch.kleis.lcaac.core.config.CacheConfig
import ch.kleis.lcaac.core.config.ConnectorConfig

object CsvConnectorKeys {
    const val CSV_CONNECTOR_NAME = "csv"
    const val CSV_CONNECTOR_KEY_DIRECTORY = "directory"

    fun defaultConfig(): ConnectorConfig =
        ConnectorConfig(
            name = CSV_CONNECTOR_NAME,
            cache = CacheConfig(
                enabled = false,
                maxSize = 1024,
            ),
            options = mapOf(
                CSV_CONNECTOR_KEY_DIRECTORY to "."
            )
        )
}
