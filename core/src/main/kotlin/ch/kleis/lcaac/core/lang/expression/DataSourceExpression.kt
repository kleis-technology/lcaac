package ch.kleis.lcaac.core.lang.expression

sealed interface ColumnType<Q>
class CText<Q> : ColumnType<Q>
data class CQuantity<Q>(val defaultValue: DataExpression<Q>): ColumnType<Q>

sealed interface DataSourceExpression<Q> {
    val schema: Map<String, ColumnType<Q>>
}

data class ECsvSource<Q>(
        val location: String,
        override val schema: Map<String, ColumnType<Q>>,
) : DataSourceExpression<Q>

