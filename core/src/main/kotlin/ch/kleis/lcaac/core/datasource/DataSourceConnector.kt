package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue

interface DataSourceConnector<Q> {
    fun getName(): String
    fun getConfig(): ConnectorConfig
    fun getFirst(
        caller: DataSourceOperations<Q>,
        config: DataSourceConfig,
        source: DataSourceValue<Q>): ERecord<Q>
    fun getAll(
        caller: DataSourceOperations<Q>,
        config: DataSourceConfig,
        source: DataSourceValue<Q>): Sequence<ERecord<Q>>
}
