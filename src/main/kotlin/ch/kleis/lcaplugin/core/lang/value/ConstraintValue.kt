package ch.kleis.lcaplugin.core.lang.value

data class FromProcessRefValue(
    val name: String,
    val matchLabels: Map<String, StringValue> = emptyMap(),
    val arguments: Map<String, DataValue> = emptyMap(),
)
