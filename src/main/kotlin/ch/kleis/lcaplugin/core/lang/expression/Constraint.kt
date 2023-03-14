package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.QuantityExpressionReducer

@optics
sealed interface Constraint {
    fun reduceWith(reducer: QuantityExpressionReducer): Constraint

    companion object
}

object None : Constraint {
    override fun reduceWith(reducer: QuantityExpressionReducer): Constraint {
        return this
    }
}

@optics
data class FromProcessRef(val template: ETemplateRef, val arguments: Map<String, QuantityExpression>) : Constraint {
    override fun reduceWith(reducer: QuantityExpressionReducer): Constraint {
        return FromProcessRef(
            template,
            arguments.mapValues { reducer.reduce(it.value) }
        )
    }

    companion object
}
