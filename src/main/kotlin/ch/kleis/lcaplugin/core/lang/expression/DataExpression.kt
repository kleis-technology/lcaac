package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics

@optics
sealed interface DataExpression : Expression {
    companion object
}

@optics
sealed interface QuantityExpression : DataExpression {
    companion object
}

@optics
sealed interface StringExpression : DataExpression {
    companion object
}
