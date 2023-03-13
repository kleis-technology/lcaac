package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.expression.*
import kotlin.math.pow

class QuantityExpressionReducer(
    quantityRegister: Register<QuantityExpression>,
    unitRegister: Register<UnitExpression>,
) : Reducer<QuantityExpression> {
    private val quantityRegister = Register(quantityRegister)
    private val unitExpressionReducer = UnitExpressionReducer(unitRegister)
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
        unitExpressionReducer.reduce(expression.unit)
    )

    private fun reducePow(expression: EQuantityPow): QuantityExpression {
        val quantity = reduce(expression.quantity)
        val exponent = expression.exponent
        if (quantity !is EQuantityLiteral) {
            return EQuantityPow(quantity, exponent)
        }
        return EQuantityLiteral(
            quantity.amount.pow(exponent),
            unitExpressionReducer.reduce(EUnitPow(quantity.unit, exponent))
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
            unitExpressionReducer.reduce(EUnitDiv(leftUnit, rightUnit)),
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
            unitExpressionReducer.reduce(EUnitMul(leftUnit, rightUnit)),
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
}
