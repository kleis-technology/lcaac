package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics

@optics
sealed interface Expression {
    companion object
}

sealed interface RefExpression {
    fun name(): String
}

sealed interface QuantityExpression
sealed interface StringExpression
