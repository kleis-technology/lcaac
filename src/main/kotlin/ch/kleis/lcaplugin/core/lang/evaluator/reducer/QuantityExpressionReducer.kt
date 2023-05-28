package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.UnitSymbol
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import kotlin.math.pow

class QuantityExpressionReducer(
    quantityRegister: Register<QuantityExpression>,
) : Reducer<QuantityExpression> {
    private val quantityRegister = Register(quantityRegister)
    private val infiniteUnitLoopChecker = InfiniteUnitLoopChecker()

    /*
        Normal form = EQuantityScale(s, EUnitLiteral(...))
     */
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
            is EUnitLiteral -> EQuantityScale(1.0, expression)
            is EUnitOf -> reduceUnitOf(expression)
        }
    }

    private fun reduceUnitOf(unitOf: EUnitOf): QuantityExpression {
        val reducedExpression = reduce(unitOf.expression)
        return when {
            reducedExpression is EQuantityScale && reducedExpression.base is EUnitLiteral -> EQuantityScale(
                1.0,
                reducedExpression.base
            )

            reducedExpression is EUnitOf -> reducedExpression
            else -> EUnitOf(reducedExpression)
        }
    }

    private fun reduceAdd(expression: EQuantityAdd): QuantityExpression {
        val left = reduce(expression.left)
        val right = reduce(expression.right)
        return when {
            left is EQuantityScale && left.base is EUnitLiteral && right is EQuantityScale && right.base is EUnitLiteral -> {
                val resultUnit = checkAndReturnUnitForAddition(left.base, right.base)
                val resultAmount = (left.scale * left.base.scale + right.scale * right.base.scale) / resultUnit.scale
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
            left is EQuantityScale && left.base is EUnitLiteral && right is EQuantityScale && right.base is EUnitLiteral ->
                EQuantityScale(
                    left.scale / right.scale,
                    EUnitLiteral(
                        left.base.symbol.divide(right.base.symbol),
                        left.base.scale / right.base.scale,
                        left.base.dimension.divide(right.base.dimension),
                    )
                )

            else -> EQuantityDiv(left, right)
        }
    }

    private fun reduceMul(expression: EQuantityMul): QuantityExpression {
        val left = reduce(expression.left)
        val right = reduce(expression.right)
        return when {
            left is EQuantityScale && left.base is EUnitLiteral && right is EQuantityScale && right.base is EUnitLiteral ->
                EQuantityScale(
                    left.scale * right.scale,
                    EUnitLiteral(
                        left.base.symbol.multiply(right.base.symbol),
                        left.base.scale * right.base.scale,
                        left.base.dimension.multiply(right.base.dimension),
                    )
                )

            else -> EQuantityMul(left, right)
        }
    }

    private fun reducePow(expression: EQuantityPow): QuantityExpression {
        val quantity = reduce(expression.quantity)
        return when {
            quantity is EQuantityScale && quantity.base is EUnitLiteral ->
                EQuantityScale(
                    quantity.scale.pow(expression.exponent),
                    EUnitLiteral(
                        quantity.base.symbol.pow(expression.exponent),
                        quantity.base.scale.pow(expression.exponent),
                        quantity.base.dimension.pow(expression.exponent),
                    ),
                )

            else -> EQuantityPow(quantity, expression.exponent)
        }
    }

    private fun reduceRef(expression: EQuantityRef) =
        quantityRegister[expression.name]?.let { reduce(it) } ?: expression

    private fun reduceScale(expression: EQuantityScale): QuantityExpression {
        return when (val base = reduce(expression.base)) {
            is EQuantityScale -> EQuantityScale(expression.scale * base.scale, base.base)
            else -> EQuantityScale(expression.scale, base)
        }
    }

    private fun reduceSub(expression: EQuantitySub): QuantityExpression {
        val left = reduce(expression.left)
        val right = reduce(expression.right)
        return when {
            left is EQuantityScale && left.base is EUnitLiteral && right is EQuantityScale && right.base is EUnitLiteral -> {
                val resultUnit = checkAndReturnUnitForAddition(left.base, right.base)
                val resultAmount = (left.scale * left.base.scale - right.scale * right.base.scale) / resultUnit.scale
                EQuantityScale(resultAmount, resultUnit)
            }

            else -> EQuantitySub(left, right)
        }
    }

    private fun reduceAlias(expression: EUnitAlias): QuantityExpression {
        val aliasForExpression = reduceAliasFor(expression)
        return when {
            aliasForExpression is EQuantityScale && aliasForExpression.base is EUnitLiteral -> {
                EQuantityScale(
                    1.0,
                    EUnitLiteral(
                        UnitSymbol.of(expression.symbol),
                        aliasForExpression.scale * aliasForExpression.base.scale,
                        aliasForExpression.base.dimension
                    )
                )
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
