package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.*
import arrow.typeclasses.Monoid
import ch.kleis.lcaplugin.core.lang.Dimension

@optics
sealed interface UnitExpression : Expression {
    companion object
}

@optics
data class EUnitLiteral(val symbol: String, val scale: Double, val dimension: Dimension) : UnitExpression {
    companion object
}
@optics
data class EUnitMul(val left: UnitExpression, val right: UnitExpression) : UnitExpression {
    companion object
}
@optics
data class EUnitDiv(val left: UnitExpression, val right: UnitExpression) : UnitExpression {
    companion object
}
@optics
data class EUnitPow(val unit: UnitExpression, val exponent: Double) : UnitExpression {
    companion object
}
@optics
data class EUnitRef(val name: String) : UnitExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name

    companion object
}

