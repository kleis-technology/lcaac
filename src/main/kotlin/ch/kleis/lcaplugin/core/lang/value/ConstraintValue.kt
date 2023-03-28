package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics

@optics
sealed interface ConstraintValue {
    companion object
}

object NoneValue : ConstraintValue

@optics
data class FromProcessRefValue(
    val name: String,
    val arguments: Map<String, QuantityValue>,
) : ConstraintValue {
    companion object
}
