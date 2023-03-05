package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*
import kotlin.math.pow

class QuantityExpressionReducer(
    quantityEnvironment: Environment<QuantityExpression>,
    unitEnvironment: Environment<UnitExpression>,
) : Reducer<QuantityExpression> {
    val quantityEnvironment = Environment(quantityEnvironment)
    private val unitExpressionReducer = UnitExpressionReducer(unitEnvironment)
    override fun reduce(expression: QuantityExpression): QuantityExpression {
        return when (expression) {
            is EQuantityAdd -> {
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

            is EQuantitySub -> {
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

            is EQuantityMul -> {
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

            is EQuantityDiv -> {
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

            is EQuantityPow -> {
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

            is EQuantityLiteral -> EQuantityLiteral(
                expression.amount,
                unitExpressionReducer.reduce(expression.unit)
            )

            is EQuantityRef -> quantityEnvironment[expression.name]?.let { reduce(it) } ?: expression
        }
    }
}
