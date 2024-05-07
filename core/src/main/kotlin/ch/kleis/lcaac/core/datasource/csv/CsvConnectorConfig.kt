package ch.kleis.lcaac.core.datasource.csv

import ch.kleis.lcaac.core.config.LcaacConnectorConfig
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
    }
}

fun LcaacConnectorConfig.csv(): CsvConnectorConfig? {
    if (this.name != CSV_CONNECTOR_NAME) {
        return null
    }
        val directory = this.options[CSV_CONNECTOR_KEY_DIRECTORY]
            ?.let { Path.of(it).toFile() }
            ?: return null
        return CsvConnectorConfig(
            directory,
        )
}
