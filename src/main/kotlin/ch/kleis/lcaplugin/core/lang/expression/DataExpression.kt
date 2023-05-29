package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol

@optics
sealed interface DataExpression : Expression {
    companion object
}

@optics
data class EDataRef(val name: String) : DataExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String {
        return name
    }

    companion object
}

/*
    Quantities
 */

@optics
data class EUnitLiteral(val symbol: UnitSymbol, val scale: Double, val dimension: Dimension) : DataExpression,
    QuantityExpression {
    override fun toString(): String {
        return symbol.toString()
    }

    companion object
}

@optics
data class EUnitAlias(val symbol: String, val aliasFor: DataExpression) : DataExpression, QuantityExpression {
    companion object
}

@optics
data class EQuantityScale(val scale: Double, val base: DataExpression) : DataExpression, QuantityExpression {
    companion object
}

@optics
data class EUnitOf(val expression: DataExpression) : DataExpression, QuantityExpression {
    companion object
}

@optics
data class EQuantityAdd(val left: DataExpression, val right: DataExpression) : DataExpression, QuantityExpression {
    companion object
}

@optics
data class EQuantitySub(val left: DataExpression, val right: DataExpression) : DataExpression, QuantityExpression {
    companion object
}

@optics
data class EQuantityMul(val left: DataExpression, val right: DataExpression) : DataExpression, QuantityExpression {
    companion object
}

@optics
data class EQuantityDiv(val left: DataExpression, val right: DataExpression) : DataExpression, QuantityExpression {
    companion object
}

@optics
data class EQuantityPow(val quantity: DataExpression, val exponent: Double) : DataExpression, QuantityExpression {
    companion object
}

@optics
data class EQuantityClosure(
    val symbolTable: SymbolTable,
    val expression: DataExpression
) : DataExpression, QuantityExpression {
    companion object
}

/*
    String
 */

@optics
data class EStringLiteral(val value: String) : DataExpression, StringExpression {
    override fun toString(): String {
        return value
    }

    companion object
}
