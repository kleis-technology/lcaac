package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.lang.evaluator.Beta
import ch.kleis.lcaplugin.core.lang.evaluator.QuantityExpressionReducer
import ch.kleis.lcaplugin.core.lang.expression.QuantityExpression

sealed interface Constraint {
    fun reduceWith(reducer: QuantityExpressionReducer): Constraint
    fun substituteWith(beta: Beta, binder: String, value: QuantityExpression): Constraint
    fun rename(existing: String, replacement: String): Constraint
}

object None : Constraint {
    override fun reduceWith(reducer: QuantityExpressionReducer): Constraint {
        return this
    }

    override fun substituteWith(beta: Beta, binder: String, value: QuantityExpression): Constraint {
        return this
    }

    override fun rename(existing: String, replacement: String): Constraint {
        return this
    }

}

data class FromProcessRef(val ref: String, val arguments: Map<String, QuantityExpression>) : Constraint {
    override fun reduceWith(reducer: QuantityExpressionReducer): Constraint {
        return FromProcessRef(
            ref,
            arguments.mapValues { reducer.reduce(it.value) }
        )
    }

    override fun substituteWith(beta: Beta, binder: String, value: QuantityExpression): Constraint {
        return FromProcessRef(
            ref,
            arguments.mapValues { beta.substitute(binder, value, it.value) },
        )
    }

    override fun rename(existing: String, replacement: String): Constraint {
        return if (ref == existing) FromProcessRef(replacement, arguments) else this
    }
}
