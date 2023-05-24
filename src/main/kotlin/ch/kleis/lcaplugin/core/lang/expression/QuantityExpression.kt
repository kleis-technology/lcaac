package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics
import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.SymbolTable

@optics
sealed interface QuantityExpression : Expression {
    companion object
}

@optics
data class EUnitLiteral(val symbol: String, val scale: Double, val dimension: Dimension) : QuantityExpression {
    override fun toString(): String {
        return symbol
    }

    companion object
}

@optics
data class EUnitAlias(val symbol: String, val aliasFor: QuantityExpression) : QuantityExpression {
    companion object
}

@optics
data class EQuantityScale(val scale: Double, val base: QuantityExpression) : QuantityExpression {
    companion object
}

@optics
data class EUnitOf(val expression: QuantityExpression) : QuantityExpression {
    companion object
}

@optics
data class EQuantityAdd(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression {
    companion object
}

@optics
data class EQuantitySub(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression {
    companion object
}

@optics
data class EQuantityNeg(val quantity: QuantityExpression) : QuantityExpression {
    companion object
}

@optics
data class EQuantityMul(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression {
    companion object
}

@optics
data class EQuantityDiv(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression {
    companion object
}

@optics
data class EQuantityPow(val quantity: QuantityExpression, val exponent: Double) : QuantityExpression {
    companion object
}

@optics
data class EQuantityClosure(val symbolTable: SymbolTable, val expression: QuantityExpression) : QuantityExpression {
    companion object
}

@optics
data class EQuantityRef(val name: String) : QuantityExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name

    companion object
}

