package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.DataSourceExpression
import ch.kleis.lcaac.core.lang.expression.ERecord

interface DataSourceOperations<Q> {
    fun readAll(source: DataSourceExpression<Q>): Sequence<ERecord<Q>>
    fun sum(source: DataSourceExpression<Q>, column: String): DataExpression<Q>
}
