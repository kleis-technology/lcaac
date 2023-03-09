package ch.kleis.lcaplugin.core.lang.expression

sealed interface Expression
sealed interface RefExpression {
    fun name(): String
}

