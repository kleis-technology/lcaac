package ch.kleis.lcaac.core.lang.value

import ch.kleis.lcaac.core.config.DataSourceConfig

data class DataSourceValue<Q>(
    val config: DataSourceConfig,
    val schema: Map<String, DataValue<Q>>,
    val filter: Map<String, DataValue<Q>> = emptyMap(),
)
