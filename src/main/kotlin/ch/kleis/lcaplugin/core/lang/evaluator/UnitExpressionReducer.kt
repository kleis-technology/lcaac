package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.expression.*
import kotlin.math.pow

class UnitExpressionReducer(environment: Register<UnitExpression>) : Reducer<UnitExpression> {
    private val environment = Register(environment)
    override fun reduce(expression: UnitExpression): UnitExpression {
        return when (expression) {
            is EUnitLiteral -> reduceLiteral(expression)
            is EUnitDiv -> reduceDiv(expression)
            is EUnitMul -> reduceMul(expression)
            is EUnitPow -> reducePow(expression)
            is EUnitRef -> reduceRef(expression)
        }
    }

    private fun reduceRef(expression: EUnitRef) =
        (environment[expression.name]?.let { reduce(it) }
            ?: expression)

    private fun reducePow(expression: EUnitPow): UnitExpression {
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

    private fun reduceMul(expression: EUnitMul): UnitExpression {
        val left = reduce(expression.left)
        val right = reduce(expression.right)
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

    private fun reduceLiteral(expression: UnitExpression) = expression
}
