package ch.kleis.lcaac.core.datasource.csv

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorConfig.Companion.CSV_CONNECTOR_KEY_DIRECTORY
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorConfig.Companion.CSV_CONNECTOR_NAME
import java.io.File
import java.nio.file.Path

data class CsvConnectorConfig(
    val directory: File,
) {
    companion object {
        const val CSV_CONNECTOR_NAME = "csv"
        const val CSV_CONNECTOR_KEY_DIRECTORY = "directory"

        fun default(): ConnectorConfig =
            ConnectorConfig(
                name = CSV_CONNECTOR_NAME,
                options = mapOf(
                    CSV_CONNECTOR_KEY_DIRECTORY to "."
                )
            )
    }
}

fun ConnectorConfig.csv(): CsvConnectorConfig? {
    if (this.name != CSV_CONNECTOR_NAME) {
        return null
    }
    val directory = this.options[CSV_CONNECTOR_KEY_DIRECTORY]
        ?.let { Path.of(it).toFile() }
        ?: Path.of(".").toFile()
    return CsvConnectorConfig(
        directory,
    )
}
