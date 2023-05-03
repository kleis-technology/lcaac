package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics

@optics
data class FromProcessRefValue(
    val name: String,
    val arguments: Map<String, QuantityValue>,
) {
    companion object
}
