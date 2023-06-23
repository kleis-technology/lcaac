package ch.kleis.lcaplugin.core.lang.value

data class FromProcessRefValue(
    val name: String,
    val matchLabels: Map<String, StringValue>,
    val arguments: Map<String, DataValue>,
)
