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

    override fun toString(): String {
        return "None"
    }
}

enum class ConstraintFlag {
    IS_DEFAULT, NONE
}

@optics
data class FromProcessRef(
    val ref: String,
    val arguments: Map<String, QuantityExpression>,
    val flag: ConstraintFlag = ConstraintFlag.NONE,
) : Constraint {
    override fun reduceWith(reducer: QuantityExpressionReducer): Constraint {
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
