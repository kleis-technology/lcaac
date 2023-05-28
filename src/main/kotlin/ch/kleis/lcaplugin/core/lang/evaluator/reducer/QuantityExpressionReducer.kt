package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import kotlin.math.pow

class QuantityExpressionReducer(
    quantityRegister: Register<QuantityExpression>,
) : Reducer<QuantityExpression> {
    private val quantityRegister = Register(quantityRegister)
    private val infiniteUnitLoopChecker = InfiniteUnitLoopChecker()

    override fun reduce(expression: QuantityExpression): QuantityExpression {
        return when (expression) {
            is EQuantityAdd -> reduceAdd(expression)
            is EQuantityClosure -> reduceClosure(expression)
            is EQuantityDiv -> reduceDiv(expression)
            is EQuantityMul -> reduceMul(expression)
            is EQuantityPow -> reducePow(expression)
            is EQuantityRef -> reduceRef(expression)
            is EQuantityScale -> reduceScale(expression)
            is EQuantitySub -> reduceSub(expression)
            is EUnitAlias -> reduceAlias(expression)
            is EUnitLiteral -> expression
            is EUnitOf -> reduceUnitOf(expression)
        }
    }

    private fun reduceUnitOf(unitOf: EUnitOf): QuantityExpression {
        val reducedExpression = reduce(unitOf.expression)
        return when {
            reducedExpression is EUnitLiteral -> reducedExpression
            reducedExpression is EQuantityScale && reducedExpression.base is EUnitLiteral -> reducedExpression.base
            reducedExpression is EUnitOf -> reducedExpression
            else -> EUnitOf(reducedExpression)
        }
    }

    private fun reduceAdd(expression: EQuantityAdd): QuantityExpression {
        val left = reduce(expression.left)
        val right = reduce(expression.right)
        return when {
            left is EUnitLiteral && right is EUnitLiteral -> {
                val resultUnit = checkAndReturnUnitForAddition(left, right)
                val resultAmount = (left.scale + right.scale) / resultUnit.scale
                EQuantityScale(resultAmount, resultUnit)
            }

            left is EQuantityScale && left.base is EUnitLiteral && right is EQuantityScale && right.base is EUnitLiteral -> {
                val resultUnit = checkAndReturnUnitForAddition(left.base, right.base)
                val resultAmount = (left.scale * left.base.scale + right.scale * right.base.scale) / resultUnit.scale
                EQuantityScale(resultAmount, resultUnit)
            }

            left is EQuantityScale && left.base is EUnitLiteral && right is EUnitLiteral -> {
                val resultUnit = checkAndReturnUnitForAddition(left.base, right)
                val resultAmount = (left.scale * left.base.scale + right.scale) / resultUnit.scale
                EQuantityScale(resultAmount, resultUnit)
            }

            left is EUnitLiteral && right is EQuantityScale && right.base is EUnitLiteral -> {
                val resultUnit = checkAndReturnUnitForAddition(left, right.base)
                val resultAmount = (left.scale + right.scale * right.base.scale) / resultUnit.scale
                EQuantityScale(resultAmount, resultUnit)
            }

            else -> EQuantityAdd(left, right)
        }
    }

    private fun reduceClosure(closure: EQuantityClosure): QuantityExpression =
        QuantityExpressionReducer(closure.symbolTable.quantities).reduce(closure.expression)

    private fun reduceDiv(expression: EQuantityDiv): QuantityExpression {
        val left = reduce(expression.left)
        val right = reduce(expression.right)
        return when {
            left is EUnitLiteral && right is EUnitLiteral ->
                EUnitLiteral(
                    "${left.symbol}/${right.symbol}",
                    left.scale / right.scale,
                    left.dimension.divide(right.dimension)
                )

            left is EQuantityScale && right is EQuantityScale ->
                reduce(EQuantityScale(
                    left.scale / right.scale,
                    reduce(EQuantityDiv(left.base, right.base))
                ))

            left is EQuantityScale ->
                reduce(EQuantityScale(
                    left.scale,
                    reduce(EQuantityDiv(left.base, right))
                ))

            else -> EQuantityDiv(left, right)
        }
    }

    private fun reduceMul(expression: EQuantityMul): QuantityExpression {
        val left = reduce(expression.left)
        val right = reduce(expression.right)
        return when {
            left is EUnitLiteral && right is EUnitLiteral ->
                EUnitLiteral(
                    "${left.symbol}.${right.symbol}",
                    left.scale * right.scale,
                    left.dimension.multiply(right.dimension),
                )

            left is EQuantityScale && right is EQuantityScale ->
                reduce(EQuantityScale(
                    left.scale * right.scale,
                    reduce(EQuantityMul(left.base, right.base))
                ))

            left is EQuantityScale ->
                reduce(EQuantityScale(
                    left.scale,
                    reduce(EQuantityMul(left.base, right))
                ))

            right is EQuantityScale ->
                reduce(EQuantityScale(
                    right.scale,
                    reduce(EQuantityMul(left, right.base))
                ))

            else -> EQuantityMul(left, right)
        }
    }

    private fun reducePow(expression: EQuantityPow): QuantityExpression =
        when (val quantity = reduce(expression.quantity)) {
            is EQuantityScale ->
                EQuantityScale(
                    quantity.scale.pow(expression.exponent),
                    reduce(EQuantityPow(quantity.base, expression.exponent))
                )

            is EUnitLiteral -> EUnitLiteral(
                "${quantity.symbol}^(${expression.exponent})",
                quantity.scale.pow(expression.exponent),
                quantity.dimension.pow(expression.exponent),
            )

            else -> EQuantityPow(quantity, expression.exponent)
        }

    private fun reduceRef(expression: EQuantityRef) =
        quantityRegister[expression.name]?.let { reduce(it) } ?: expression

    private fun reduceScale(expression: EQuantityScale): QuantityExpression {
        val base = reduce(expression.base)
        return when {
            expression.scale == 1.0 -> base
            base is EQuantityScale -> EQuantityScale(expression.scale * base.scale, base.base)
            else -> EQuantityScale(expression.scale, base)
        }
    }

    private fun reduceSub(expression: EQuantitySub): QuantityExpression {
        val left = reduce(expression.left)
        val right = reduce(expression.right)
        return when {
            left is EUnitLiteral && right is EUnitLiteral -> {
                val resultUnit = checkAndReturnUnitForAddition(left, right)
                val resultAmount = (left.scale - right.scale) / resultUnit.scale
                EQuantityScale(resultAmount, resultUnit)
            }

            left is EQuantityScale && left.base is EUnitLiteral && right is EQuantityScale && right.base is EUnitLiteral -> {
                val resultUnit = checkAndReturnUnitForAddition(left.base, right.base)
                val resultAmount = (left.scale * left.base.scale - right.scale * right.base.scale) / resultUnit.scale
                EQuantityScale(resultAmount, resultUnit)
            }

            left is EQuantityScale && left.base is EUnitLiteral && right is EUnitLiteral -> {
                val resultUnit = checkAndReturnUnitForAddition(left.base, right)
                val resultAmount = (left.scale * left.base.scale - right.scale) / resultUnit.scale
                EQuantityScale(resultAmount, resultUnit)
            }

            left is EUnitLiteral && right is EQuantityScale && right.base is EUnitLiteral -> {
                val resultUnit = checkAndReturnUnitForAddition(left, right.base)
                val resultAmount = (left.scale + right.scale - right.base.scale) / resultUnit.scale
                EQuantityScale(resultAmount, resultUnit)
            }

            else -> EQuantitySub(left, right)
        }
    }

    private fun reduceAlias(expression: EUnitAlias): QuantityExpression {
        val aliasForExpression = reduceAliasFor(expression)
        return when {
            aliasForExpression is EUnitLiteral ->
                EUnitLiteral(expression.symbol, aliasForExpression.scale, aliasForExpression.dimension)

            aliasForExpression is EQuantityScale && aliasForExpression.base is EUnitLiteral -> {
                EUnitLiteral(expression.symbol, aliasForExpression.scale * aliasForExpression.base.scale, aliasForExpression.base.dimension)
            }

            else -> EUnitAlias(expression.symbol, aliasForExpression)
        }
    }

    private fun reduceAliasFor(expression: EUnitAlias): QuantityExpression {
        infiniteUnitLoopChecker.check(expression)
        val aliasForExpression = reduce(expression.aliasFor)
        infiniteUnitLoopChecker.clearTraceAlias()

        return aliasForExpression
    }

    private fun checkAndReturnUnitForAddition(left: EUnitLiteral, right: EUnitLiteral): EUnitLiteral {
        if (left.dimension != right.dimension) {
            throw EvaluatorException("incompatible dimensions: ${left.dimension} vs ${right.dimension} in left=$left and right=$right")
        }

        return if (left.scale > right.scale) left else right
    }
}
