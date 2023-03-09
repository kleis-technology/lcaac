package ch.kleis.lcaplugin.core.lang.expression

import ch.kleis.lcaplugin.core.lang.Dimension

sealed interface UnitExpression : Expression
data class EUnitLiteral(val symbol: String, val scale: Double, val dimension: Dimension) : UnitExpression
data class EUnitMul(val left: UnitExpression, val right: UnitExpression) : UnitExpression
data class EUnitDiv(val left: UnitExpression, val right: UnitExpression) : UnitExpression
data class EUnitPow(val unit: UnitExpression, val exponent: Double) : UnitExpression
data class EUnitRef(val name: String) : UnitExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name
}

