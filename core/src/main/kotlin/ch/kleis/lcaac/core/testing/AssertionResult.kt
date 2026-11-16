package ch.kleis.lcaac.core.testing

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

sealed interface AssertionResult

data class RangeAssertionSuccess(
    val name: String,
    val lo: QuantityValue<BasicNumber>,
    val hi: QuantityValue<BasicNumber>,
    val actual: QuantityValue<BasicNumber>,
) : AssertionResult

data class GenericFailure(val message: String) : AssertionResult {
    /** The evaluation error behind this failure, with its frames. Not part of equality. */
    var error: EvaluatorException? = null
        private set

    constructor(message: String, error: EvaluatorException?) : this(message) {
        this.error = error
    }
}

data class RangeAssertionFailure(
    val name: String,
    val lo: QuantityValue<BasicNumber>,
    val hi: QuantityValue<BasicNumber>,
    val actual: QuantityValue<BasicNumber>,
) : AssertionResult
