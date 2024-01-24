package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue

interface DataSourceOperations<Q> {
    fun readAll(source: DataSourceValue<Q>): Sequence<ERecord<Q>>
    fun sumProduct(source: DataSourceValue<Q>, columns: List<String>): DataExpression<Q>
}
