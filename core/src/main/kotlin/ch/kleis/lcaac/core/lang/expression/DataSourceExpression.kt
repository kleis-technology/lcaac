package ch.kleis.lcaac.core.lang.expression

data class ColumnType<Q>(
        val defaultValue: DataExpression<Q>
)

sealed interface DataSourceExpression<Q> {
    val schema: Map<String, ColumnType<Q>>
}

data class ECsvSource<Q>(
        val location: String,
        override val schema: Map<String, ColumnType<Q>>,
) : DataSourceExpression<Q>

