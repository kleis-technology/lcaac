package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import kotlin.math.pow

class QuantityExpressionReducer(
    quantityRegister: Register<QuantityExpression>,
    unitRegister: Register<UnitExpression>,
) : Reducer<QuantityExpression> {
    private val unitRegister = Register(unitRegister)
    private val quantityRegister = Register(quantityRegister)
    private val infiniteLoopChecker = InfiniteLoopChecker()

    override fun reduce(expression: QuantityExpression): QuantityExpression {
        return when (expression) {
            is EQuantityAdd -> reduceAdd(expression)
            is EQuantitySub -> reduceSub(expression)
            is EQuantityMul -> reduceMul(expression)
            is EQuantityDiv -> reduceDiv(expression)
            is EQuantityPow -> reducePow(expression)
            is EQuantityLiteral -> reduceLiteral(expression)
            is EQuantityRef -> reduceRef(expression)
            is EQuantityNeg -> reduceNeg(expression)
            is EQuantityScale -> reduceScale(expression)
        }
    }

    fun reduceUnit(expression: UnitExpression): UnitExpression {
        return when(expression) {
            is EUnitClosure -> {
                val reducer = QuantityExpressionReducer(
                    expression.symbolTable.quantities,
                    expression.symbolTable.units,
                )
                return reducer.reduceUnit(expression.expression)
            }
            is EUnitDiv -> reduceDiv(expression)
            is EUnitLiteral -> reduceLiteral(expression)
            is EUnitMul -> reduceMul(expression)
            is EUnitOf -> when (val q = reduce(expression.quantity)) {
                is EQuantityLiteral -> q.unit
                else -> EUnitOf(q)
            }
            is EUnitPow -> reducePow(expression)
            is EUnitRef -> reduceRef(expression)
            is EUnitAlias -> reduceAlias(expression)
        }
    }

    private fun reduceAlias(expression: EUnitAlias): UnitExpression {
        infiniteLoopChecker.check(expression)
        infiniteLoopChecker.traceAlias(expression)
        val aliasForExpression = reduce(expression.aliasFor)
        infiniteLoopChecker.clearTraceAlias()

        return when (aliasForExpression) {
            is EQuantityLiteral -> {
                when (val unitAlias = reduceUnit(aliasForExpression.unit)){
                    is EUnitLiteral -> {
                        EUnitLiteral(expression.symbol, aliasForExpression.amount*unitAlias.scale, unitAlias.dimension)
                    }
                    !is EUnitLiteral -> {
                        EUnitAlias(expression.symbol, aliasForExpression)
                    }
                }
            }
            !is EQuantityLiteral -> {
                EUnitAlias(expression.symbol, aliasForExpression)
            }
        }
    }

    private fun reduceRef(expression: EQuantityRef) =
        quantityRegister[expression.name]?.let { reduce(it) } ?: expression

    private fun reduceNeg(expression: EQuantityNeg): QuantityExpression {
        val quantity = reduce(expression.quantity)
        if (quantity !is EQuantityLiteral) {
            return EQuantityNeg(quantity)
        }
        return EQuantityLiteral(
            -quantity.amount,
            quantity.unit,
        )
    }

    private fun reduceLiteral(expression: EQuantityLiteral) = EQuantityLiteral(
        expression.amount,
        reduceUnit(expression.unit)
    )

    private fun reduceScale(expression: EQuantityScale): QuantityExpression {
        val quantity = reduce(expression.quantity)
        val scale = expression.scale
        if (quantity !is EQuantityLiteral) {
            return EQuantityScale(scale, quantity)
        }
        return EQuantityLiteral(
            scale * quantity.amount,
            quantity.unit,
        )
    }

    private fun reducePow(expression: EQuantityPow): QuantityExpression {
        val quantity = reduce(expression.quantity)
        val exponent = expression.exponent
        if (quantity !is EQuantityLiteral) {
            return EQuantityPow(quantity, exponent)
        }
        return EQuantityLiteral(
            quantity.amount.pow(exponent),
            reduceUnit(EUnitPow(quantity.unit, exponent))
        )
    }

    private fun reduceDiv(expression: EQuantityDiv): QuantityExpression {
        val left = reduce(expression.left)
        val right = reduce(expression.right)
        if (left !is EQuantityLiteral) {
            return EQuantityDiv(left, right)
        }
        if (right !is EQuantityLiteral) {
            return EQuantityDiv(left, right)
        }
        val leftUnit = left.unit
        if (leftUnit !is EUnitLiteral) {
            return EQuantityDiv(left, right)
        }
        val rightUnit = right.unit
        if (rightUnit !is EUnitLiteral) {
            return EQuantityDiv(right, right)
        }
        return EQuantityLiteral(
            left.amount / right.amount,
            reduceUnit(EUnitDiv(leftUnit, rightUnit)),
        )
    }

    private fun reduceMul(expression: EQuantityMul): QuantityExpression {
        val left = reduce(expression.left)
        val right = reduce(expression.right)
        if (left !is EQuantityLiteral) {
            return EQuantityMul(left, right)
        }
        if (right !is EQuantityLiteral) {
            return EQuantityMul(left, right)
        }
        val leftUnit = left.unit
        if (leftUnit !is EUnitLiteral) {
            return EQuantityMul(left, right)
        }
        val rightUnit = right.unit
        if (rightUnit !is EUnitLiteral) {
            return EQuantityMul(right, right)
        }
        return EQuantityLiteral(
            left.amount * right.amount,
            reduceUnit(EUnitMul(leftUnit, rightUnit)),
        )
    }

    private fun reduceSub(expression: EQuantitySub): QuantityExpression {
        val left = reduce(expression.left)
        val right = reduce(expression.right)
        if (left !is EQuantityLiteral) {
            return EQuantitySub(left, right)
        }
        if (right !is EQuantityLiteral) {
            return EQuantitySub(left, right)
        }
        val leftUnit = left.unit
        if (leftUnit !is EUnitLiteral) {
            return EQuantitySub(left, right)
        }
        val rightUnit = right.unit
        if (rightUnit !is EUnitLiteral) {
            return EQuantitySub(right, right)
        }

        if (leftUnit.dimension != rightUnit.dimension) {
            throw EvaluatorException("incompatible dimensions: ${leftUnit.dimension} vs ${rightUnit.dimension}")
        }

        val resultUnit = if (leftUnit.scale > rightUnit.scale) leftUnit else rightUnit
        val resultAmount = (left.amount * leftUnit.scale - right.amount * rightUnit.scale) / resultUnit.scale
        return EQuantityLiteral(
            resultAmount,
            resultUnit,
        )
    }

    private fun reduceAdd(expression: EQuantityAdd): QuantityExpression {
        val left = reduce(expression.left)
        val right = reduce(expression.right)
        if (left !is EQuantityLiteral) {
            return EQuantityAdd(left, right)
        }
        if (right !is EQuantityLiteral) {
            return EQuantityAdd(left, right)
        }
        val leftUnit = left.unit
        if (leftUnit !is EUnitLiteral) {
            return EQuantityAdd(left, right)
        }
        val rightUnit = right.unit
        if (rightUnit !is EUnitLiteral) {
            return EQuantityAdd(right, right)
        }

        if (leftUnit.dimension != rightUnit.dimension) {
            throw EvaluatorException("incompatible dimensions: ${leftUnit.dimension} vs ${rightUnit.dimension}")
        }

        val resultUnit = if (leftUnit.scale > rightUnit.scale) leftUnit else rightUnit
        val resultAmount = (left.amount * leftUnit.scale + right.amount * rightUnit.scale) / resultUnit.scale
        return EQuantityLiteral(
            resultAmount,
            resultUnit,
        )
    }

    private fun reduceRef(expression: EUnitRef) =
        (unitRegister[expression.name]?.let { reduceUnit(it) }
            ?: expression)

    private fun reducePow(expression: EUnitPow): UnitExpression {
        val unit = reduceUnit(expression.unit)
        val exponent = expression.exponent
        if (unit !is EUnitLiteral) {
            return EUnitPow(unit, exponent)
        }
        return EUnitLiteral(
            "${unit.symbol}^($exponent)",
            unit.scale.pow(exponent),
            unit.dimension.pow(exponent),
        )
    }

    private fun reduceMul(expression: EUnitMul): UnitExpression {
        val left = reduceUnit(expression.left)
        val right = reduceUnit(expression.right)
        if (left !is EUnitLiteral) {
            return EUnitMul(left, right)
        }
        if (right !is EUnitLiteral) {
            return EUnitMul(left, right)
        }
        return EUnitLiteral(
            "${left.symbol}.${right.symbol}",
            left.scale * right.scale,
            left.dimension.multiply(right.dimension)
        )
    }

    private fun reduceDiv(expression: EUnitDiv): UnitExpression {
        val left = reduceUnit(expression.left)
        val right = reduceUnit(expression.right)
        if (left !is EUnitLiteral) {
            return EUnitDiv(left, right)
        }
        if (right !is EUnitLiteral) {
            return EUnitDiv(left, right)
        }
        return EUnitLiteral(
            "${left.symbol}/${right.symbol}",
            left.scale / right.scale,
            left.dimension.divide(right.dimension)
        )
    }

    private fun reduceLiteral(expression: UnitExpression) = expression
}
