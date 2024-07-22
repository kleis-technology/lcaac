package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.cache.CachedConnector
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.math.QuantityOperations

class ConnectorFactory<Q>(
    private val workingDirectory: String,
    private val lcaacConfig: LcaacConfig,
    private val ops: QuantityOperations<Q>,
    private val builders: List<ConnectorBuilder<Q>> = listOf(CsvConnectorBuilder())
) {

    fun getWorkingDirectory(): String = workingDirectory
    fun getLcaacConfig(): LcaacConfig = lcaacConfig
    fun getQuantityOperations(): QuantityOperations<Q> = ops

    fun buildOrNull(config: ConnectorConfig): DataSourceConnector<Q>? {
        return builders.firstNotNullOfOrNull {
            if (config.cache.enabled)
                it.buildOrNull(this, config)
                    ?.let { c -> CachedConnector(c, config.cache.maxSize) }
            else it.buildOrNull(this, config)
        }
    }
}
