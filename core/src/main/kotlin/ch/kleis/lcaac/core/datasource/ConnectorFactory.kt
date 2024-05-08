package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.math.QuantityOperations

class ConnectorFactory<Q>(
    private val lcaacConfig: LcaacConfig,
    private val ops: QuantityOperations<Q>,
    builders: List<ConnectorBuilder<Q>> = listOf(CsvConnectorBuilder())
) {
    private val builders = ArrayList<ConnectorBuilder<Q>>(builders)

    fun getLcaacConfig(): LcaacConfig = lcaacConfig
    fun getQuantityOperations(): QuantityOperations<Q> = ops

    fun register(builder: ConnectorBuilder<Q>) {
        builders.add(builder)
    }

    fun buildOrNull(config: ConnectorConfig): DataSourceConnector<Q>? {
        return builders.firstNotNullOfOrNull { it.buildOrNull(this, config) }
    }
}
