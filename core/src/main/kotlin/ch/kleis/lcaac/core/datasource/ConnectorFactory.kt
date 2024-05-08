package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.datasource.csv.CsvConnector
import ch.kleis.lcaac.core.datasource.csv.csv
import ch.kleis.lcaac.core.math.QuantityOperations

interface ConnectorFactory<Q> {
    fun buildOrNull(config: ConnectorConfig): DataSourceConnector<Q>?

    companion object {
        fun <Q> default(ops: QuantityOperations<Q>) = object : ConnectorFactory<Q> {
            override fun buildOrNull(config: ConnectorConfig): DataSourceConnector<Q>? {
                return config
                    .csv()?.let { CsvConnector(it, ops) }
                // add other cases here
            }
        }
    }
}
