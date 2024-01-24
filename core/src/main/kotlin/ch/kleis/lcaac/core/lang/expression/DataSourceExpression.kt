package ch.kleis.lcaac.core.lang.expression

data class ColumnType<Q>(
        val defaultValue: DataExpression<Q>
)

sealed interface DataSourceExpression<Q>

data class EDataSource<Q> (
    val location: String,
    val schema: Map<String, ColumnType<Q>>,
) : DataSourceExpression<Q>

data class EDataSourceRef<Q>(
    val name: String
) : DataSourceExpression<Q>

