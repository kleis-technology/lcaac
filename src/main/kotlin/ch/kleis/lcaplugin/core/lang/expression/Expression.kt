package ch.kleis.lcaplugin.core.lang.expression

sealed interface Expression<Q> {
    companion object
}

sealed interface RefExpression {
    fun name(): String
}

sealed interface QuantityExpression<Q>
sealed interface StringExpression
