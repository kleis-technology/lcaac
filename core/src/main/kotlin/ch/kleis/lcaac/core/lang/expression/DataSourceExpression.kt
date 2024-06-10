package ch.kleis.lcaac.core.lang.expression

import ch.kleis.lcaac.core.config.DataSourceConfig

sealed interface DataSourceExpression<Q>

data class EDataSource<Q> (
    val config: DataSourceConfig,
    val schema: Map<String, DataExpression<Q>>,
    val filter: Map<String, DataExpression<Q>> = emptyMap(),
) : DataSourceExpression<Q>

data class EDataSourceRef<Q>(
    val name: String
) : DataSourceExpression<Q>

data class EFilter<Q>(
    val dataSource: DataSourceExpression<Q>,
    val filter: Map<String, DataExpression<Q>>
) : DataSourceExpression<Q>
