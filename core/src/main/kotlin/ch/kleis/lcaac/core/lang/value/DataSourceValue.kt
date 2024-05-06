package ch.kleis.lcaac.core.lang.value

data class DataSourceValue<Q>(
    val name: String,
    val location: String,
    val schema: Map<String, DataValue<Q>>,
    val filter: Map<String, DataValue<Q>> = emptyMap(),
)
