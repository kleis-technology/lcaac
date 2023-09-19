package ch.kleis.lcaac.core.lang.value

data class FromProcessRefValue<Q>(
    val name: String,
    val matchLabels: Map<String, StringValue<Q>> = emptyMap(),
    val arguments: Map<String, DataValue<Q>> = emptyMap(),
)
