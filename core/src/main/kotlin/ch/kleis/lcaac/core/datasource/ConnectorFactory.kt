package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.datasource.resilio_db.ResilioDbConnectorBuilder
import ch.kleis.lcaac.core.math.QuantityOperations

class ConnectorFactory<Q>(
    private val workingDirectory: String,
    private val lcaacConfig: LcaacConfig,
    private val ops: QuantityOperations<Q>,
    private val builders: List<ConnectorBuilder<Q>> = listOf(CsvConnectorBuilder(), ResilioDbConnectorBuilder())
) {

    fun getWorkingDirectory(): String = workingDirectory
    fun getLcaacConfig(): LcaacConfig = lcaacConfig
    fun getQuantityOperations(): QuantityOperations<Q> = ops

    private fun buildOrNull(config: ConnectorConfig): DataSourceConnector<Q>? {
        return builders.firstNotNullOfOrNull {
            it.buildOrNull(this, config)
        }
    }

    fun buildConnectors(): Map<String, DataSourceConnector<Q>> {
        return lcaacConfig.connectors.mapNotNull {
            this.buildOrNull(it)
        }.associateBy { it.getName() }
    }
}
