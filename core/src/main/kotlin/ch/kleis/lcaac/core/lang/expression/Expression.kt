package ch.kleis.lcaac.core.lang.expression

sealed interface Expression<Q> {
    companion object
}

sealed interface RefExpression {
    fun name(): String
}

sealed interface QuantityExpression<Q>
sealed interface StringExpression
