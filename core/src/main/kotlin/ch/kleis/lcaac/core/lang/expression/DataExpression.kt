package ch.kleis.lcaac.core.lang.expression

import arrow.optics.optics
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol

@optics
sealed interface DataExpression<Q> {
    companion object
}

sealed interface QuantityExpression<Q>
sealed interface StringExpression

/*
    Record
 */

@optics
data class EDefaultRecordOf<Q>(val dataSource: DataSourceExpression<Q>) : DataExpression<Q> {
    companion object
}

@optics
data class ERecord<Q>(val entries: Map<String, DataExpression<Q>>) : DataExpression<Q> {
    companion object
}

@optics
data class ERecordEntry<Q>(val record: DataExpression<Q>, val index: String) : DataExpression<Q> {
    companion object
}

/*
    Column operations
 */

sealed interface ColumnOperationExpression<Q>

@optics
data class ESumProduct<Q>(val dataSource: DataSourceExpression<Q>, val columns: List<String>)
    : DataExpression<Q>, ColumnOperationExpression<Q> {
    companion object
}

/*
    Ref
 */

@optics
data class EDataRef<Q>(val name: String) : DataExpression<Q> {
    fun name(): String {
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
data class EUnitLiteral<Q>(val symbol: UnitSymbol, val scale: Double, val dimension: Dimension) : DataExpression<Q>,
    QuantityExpression<Q> {
    override fun toString(): String {
        return symbol.toString()
    }

    companion object
}

@optics
data class EUnitAlias<Q>(val symbol: String, val aliasFor: DataExpression<Q>) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantityScale<Q>(val scale: Q, val base: DataExpression<Q>) : DataExpression<Q>, QuantityExpression<Q> {
    override fun toString(): String {
        return "$scale $base"
    }

    companion object
}

@optics
data class EUnitOf<Q>(val expression: DataExpression<Q>) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantityAdd<Q>(val leftHandSide: DataExpression<Q>, val rightHandSide: DataExpression<Q>) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantitySub<Q>(val leftHandSide: DataExpression<Q>, val rightHandSide: DataExpression<Q>) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantityMul<Q>(val leftHandSide: DataExpression<Q>, val rightHandSide: DataExpression<Q>) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantityDiv<Q>(val leftHandSide: DataExpression<Q>, val rightHandSide: DataExpression<Q>) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantityPow<Q>(val quantity: DataExpression<Q>, val exponent: Double) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantityClosure<Q>(
    val symbolTable: SymbolTable<Q>, val expression: DataExpression<Q>
) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

/*
    String
 */

@optics
data class EStringLiteral<Q>(val value: String) : DataExpression<Q>, StringExpression {
    override fun toString(): String {
        return value
    }

    companion object
}
