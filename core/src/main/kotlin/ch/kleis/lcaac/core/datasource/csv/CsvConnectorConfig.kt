package ch.kleis.lcaac.core.datasource.csv

import ch.kleis.lcaac.core.config.CacheConfig
import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorConfig.Companion.CSV_CONNECTOR_KEY_DIRECTORY
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorConfig.Companion.CSV_CONNECTOR_NAME
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

data class CsvConnectorConfig(
    val directory: File,
) {
    companion object {
        const val CSV_CONNECTOR_NAME = "csv"
        const val CSV_CONNECTOR_KEY_DIRECTORY = "directory"

        fun default(): ConnectorConfig =
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
}

fun ConnectorConfig.csv(workingDirectory: String): CsvConnectorConfig? {
    if (this.name != CSV_CONNECTOR_NAME) {
        return null
    }
    val directory = this.options[CSV_CONNECTOR_KEY_DIRECTORY]
        ?.let {
            val directoryPath = Path.of(it)
            if (directoryPath.isAbsolute) directoryPath.toFile()
            else Paths.get(workingDirectory, it).toFile()
        }
        ?: Path.of(workingDirectory).toFile()
    return CsvConnectorConfig(
        directory,
    )
}
