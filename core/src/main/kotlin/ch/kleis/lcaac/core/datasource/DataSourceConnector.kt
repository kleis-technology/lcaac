package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.LcaacConnectorConfig
import ch.kleis.lcaac.core.config.LcaacDataSourceConfig
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue

interface DataSourceConnector<Q> {
    fun getFirst(config: LcaacDataSourceConfig, source: DataSourceValue<Q>): ERecord<Q>
    fun getAll(config: LcaacDataSourceConfig, source: DataSourceValue<Q>): Sequence<ERecord<Q>>
}
