package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics
import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.SymbolTable

@optics
sealed interface UnitExpression : Expression {
    companion object
}

@optics
data class EUnitOf(val quantity: QuantityExpression) : UnitExpression {
    companion object
}

@optics
data class EUnitClosure(val symbolTable: SymbolTable, val expression: UnitExpression): UnitExpression {
    companion object
}

@optics
data class EUnitLiteral(val symbol: String, val scale: Double, val dimension: Dimension) : UnitExpression {
    override fun toString(): String {
        return symbol
    }
    companion object
}

@optics
data class EUnitAlias(val symbol: String, val aliasFor: QuantityExpression) : UnitExpression {
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

