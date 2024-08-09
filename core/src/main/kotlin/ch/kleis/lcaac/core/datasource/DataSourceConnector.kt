package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.value.DataSourceValue

interface DataSourceConnector<Q> {
    fun getName(): String
    fun getFirst(config: DataSourceConfig, source: DataSourceValue<Q>): ERecord<Q>
    fun getAll(config: DataSourceConfig, source: DataSourceValue<Q>): Sequence<ERecord<Q>>
    fun sumProduct(config: DataSourceConfig, source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q>
}
