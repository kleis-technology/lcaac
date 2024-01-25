package ch.kleis.lcaac.core.lang.expression.optics

import arrow.optics.Every
import arrow.typeclasses.Monoid
import ch.kleis.lcaac.core.lang.expression.*

fun <Q> everyDataExpressionInDataSourceExpression() = object : Every<DataSourceExpression<Q>, DataExpression<Q>> {
    override fun <R> foldMap(M: Monoid<R>, source: DataSourceExpression<Q>, map: (focus: DataExpression<Q>) -> R): R {
        return when (source) {
            is EDataSource -> M.fold(
                source.schema.values.plus(source.filter.values).map(map)
            )

            is EDataSourceRef -> M.empty()
            is EFilter -> M.fold(
                source.filter.values.map(map)
                    .plus(
                        foldMap(M, source.dataSource, map)
                    )
            )
        }
    }

    override fun modify(source: DataSourceExpression<Q>, map: (focus: DataExpression<Q>) -> DataExpression<Q>): DataSourceExpression<Q> {
        return when (source) {
            is EDataSource -> source.copy(
                schema = source.schema.mapValues { map(it.value) },
                filter = source.filter.mapValues { map(it.value) },
            )
            is EDataSourceRef -> source
            is EFilter -> EFilter(
                modify(source.dataSource, map),
                source.filter.mapValues { map(it.value) }
            )
        }
    }

}
