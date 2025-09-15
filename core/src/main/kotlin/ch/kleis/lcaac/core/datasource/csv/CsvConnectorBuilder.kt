package ch.kleis.lcaac.core.datasource.csv

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.datasource.ConnectorBuilder
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class CsvConnectorBuilder<Q> : ConnectorBuilder<Q> {
    companion object {
        fun directory(workingDirectory: String, config: ConnectorConfig): File {
            return config.options[CsvConnectorKeys.CSV_CONNECTOR_KEY_DIRECTORY]
                ?.let {
                    val directoryPath = Path.of(it)
                    if (directoryPath.isAbsolute) directoryPath.toFile()
                    else Paths.get(workingDirectory, it).toFile()
                }
                ?: Path.of(workingDirectory).toFile()
        }
    }

    override fun buildOrNull(factory: ConnectorFactory<Q>, config: ConnectorConfig): DataSourceConnector<Q>? {
        if (config.name != CsvConnectorKeys.CSV_CONNECTOR_NAME) {
            return null
        }
        val directory = directory(factory.getWorkingDirectory(), config)
        return CsvConnector(
            config,
            factory.getQuantityOperations()
        ) { location ->
            val csvFile = Paths.get(directory.absolutePath, location)
            csvFile.toFile().inputStream()
        }
    }
}
