package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.lang.expression.DataSourceExpression
import ch.kleis.lcaac.core.lang.expression.EMap

interface DataSourceOperations<Q> {
    fun readAll(source: DataSourceExpression<Q>): Sequence<EMap<Q>>
}
