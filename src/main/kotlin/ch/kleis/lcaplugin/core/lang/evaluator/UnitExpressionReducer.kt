package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*
import kotlin.math.pow

class UnitExpressionReducer(environment: Environment<UnitExpression>) : Reducer<UnitExpression> {
    private val environment = Environment(environment)
    override fun reduce(expression: UnitExpression): UnitExpression {
        return when (expression) {
            is EUnitLiteral -> expression
            is EUnitDiv -> {
                val left = reduce(expression.left)
                val right = reduce(expression.right)
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

            is EUnitMul -> {
                val left = reduce(expression.left)
                val right = reduce(expression.right)
                if (left !is EUnitLiteral) {
                    return EUnitDiv(left, right)
                }
                if (right !is EUnitLiteral) {
                    return EUnitDiv(left, right)
                }
                return EUnitLiteral(
                    "${left.symbol}.${right.symbol}",
                    left.scale * right.scale,
                    left.dimension.multiply(right.dimension)
                )
            }

            is EUnitPow -> {
                val unit = reduce(expression.unit)
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

            is EUnitRef -> environment[expression.name]?.let { reduce(it) }
                ?: expression
        }
    }
}
