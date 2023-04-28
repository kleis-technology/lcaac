package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.QuantityExpressionReducer

@optics
data class FromProcessRef(
    val ref: String,
    val arguments: Map<String, QuantityExpression>,
) {
    fun reduceWith(reducer: QuantityExpressionReducer): FromProcessRef {
        return FromProcessRef(
            ref,
            arguments.mapValues { reducer.reduce(it.value) }
        )
    }

    override fun toString(): String {
        return "from $ref$arguments"
    }

    companion object
}
