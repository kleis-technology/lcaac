package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue

interface DataSourceOperations<Q> {
    fun getFirst(source: DataSourceValue<Q>): ERecord<Q>
    fun getAll(source: DataSourceValue<Q>): Sequence<ERecord<Q>>
    fun sumProduct(source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q>
}

interface DataSourceOperationsWithConfig<Q> : DataSourceOperations<Q> {
    fun getConfig(): LcaacConfig
}
