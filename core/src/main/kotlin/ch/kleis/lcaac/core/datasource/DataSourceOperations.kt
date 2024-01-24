package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EDataSource
import ch.kleis.lcaac.core.lang.expression.ERecord

interface DataSourceOperations<Q> {
    fun readAll(source: EDataSource<Q>): Sequence<ERecord<Q>>
    fun sumProduct(source: EDataSource<Q>, columns: List<String>): DataExpression<Q>
}
