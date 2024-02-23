package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.lang.value.DataValue

data class DataSourceDescription<Q>(
    val location: String,
    val schema: Map<String, DataValue<Q>>,
)
