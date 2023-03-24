package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics
import ch.kleis.lcaplugin.core.lang.expression.ConstraintFlag

@optics
sealed interface ConstraintValue {
    companion object
}

object NoneValue : ConstraintValue

@optics
data class FromProcessRefValue(
    val name: String,
    val arguments: Map<String, QuantityValue>,
    val flag: ConstraintFlag = ConstraintFlag.NONE,
) : ConstraintValue {
    companion object
}
