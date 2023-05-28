package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics

@optics
data class FromProcessRefValue(
    val name: String,
    val matchLabels: Map<String, StringValue>,
    val arguments: Map<String, DataValue>,
) {
    companion object
}
