package ch.kleis.lcaac.core.lang.expression

data class ColumnType<Q>(
        val defaultValue: DataExpression<Q>
)

sealed interface DataSourceExpression<Q>

data class EDataSource<Q> (
    val location: String,
    val schema: Map<String, ColumnType<Q>>,
    val filter: Map<String, DataExpression<Q>> = emptyMap(),
) : DataSourceExpression<Q>

data class EDataSourceRef<Q>(
    val name: String
) : DataSourceExpression<Q>

data class EFilter<Q>(
    val dataSource: DataSourceExpression<Q>,
    val filter: Map<String, DataExpression<Q>>
) : DataSourceExpression<Q>
