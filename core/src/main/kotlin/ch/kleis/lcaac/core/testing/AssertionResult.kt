package ch.kleis.lcaac.core.testing

import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

sealed interface AssertionResult

data class RangeAssertionSuccess(
    val name: String,
    val lo: QuantityValue<BasicNumber>,
    val hi: QuantityValue<BasicNumber>,
    val actual: QuantityValue<BasicNumber>,
) : AssertionResult

data class GenericFailure(val message: String) : AssertionResult

data class RangeAssertionFailure(
    val name: String,
    val lo: QuantityValue<BasicNumber>,
    val hi: QuantityValue<BasicNumber>,
    val actual: QuantityValue<BasicNumber>,
) : AssertionResult
