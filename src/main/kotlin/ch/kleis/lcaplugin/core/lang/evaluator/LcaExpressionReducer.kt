package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*

class LcaExpressionReducer(
    processEnvironment: Environment<LcaProcessExpression> = Environment.empty(),
    productEnvironment: Environment<LcaUnconstrainedProductExpression> = Environment.empty(),
    substanceEnvironment: Environment<LcaSubstanceExpression> = Environment.empty(),
    indicatorEnvironment: Environment<LcaIndicatorExpression> = Environment.empty(),
    quantityEnvironment: Environment<QuantityExpression> = Environment.empty(),
    unitEnvironment: Environment<UnitExpression> = Environment.empty(),
) : Reducer<LcaExpression> {
    private val processEnvironment = Environment(processEnvironment)
    private val productEnvironment = Environment(productEnvironment)
    private val substanceEnvironment = Environment(substanceEnvironment)
    private val indicatorEnvironment = Environment(indicatorEnvironment)

    private val unitExpressionReducer = UnitExpressionReducer(unitEnvironment)
    private val quantityExpressionReducer = QuantityExpressionReducer(quantityEnvironment, unitEnvironment)

    override fun reduce(expression: LcaExpression): LcaExpression {
        return when (expression) {
            is EProcess -> reduceLcaProcessExpression(expression)
            is EProcessRef -> reduceLcaProcessExpression(expression)

            is EImpact -> reduceImpact(expression)

            is ETechnoExchange -> reduceTechnoExchange(expression)
            is EBioExchange -> reduceBioExchange(expression)

            is EIndicator -> reduceIndicatorExpression(expression)
            is EIndicatorRef -> reduceIndicatorExpression(expression)

            is ESubstance -> reduceSubstanceExpression(expression)
            is ESubstanceRef -> reduceSubstanceExpression(expression)

            is EProduct -> reduceUnconstrainedProductExpression(expression)
            is EProductRef -> reduceUnconstrainedProductExpression(expression)
            is EConstrainedProduct -> EConstrainedProduct(
                reduceUnconstrainedProductExpression(expression.product),
                expression.constraint.reduceWith(quantityExpressionReducer),
            )

            is ESubstanceCharacterization -> reduceSubstanceCharacterization(expression)
        }
    }

    private fun reduceSubstanceCharacterization(expression: ESubstanceCharacterization): ESubstanceCharacterization {
        return ESubstanceCharacterization(
            reduceBioExchange(expression.referenceExchange),
            expression.impacts.map { reduceImpact(it) },
        )
    }


    private fun reduceLcaProcessExpression(expression: LcaProcessExpression): LcaProcessExpression {
        return when (expression) {
            is EProcess -> EProcess(
                expression.products.map { reduceTechnoExchange(it) },
                expression.inputs.map { reduceTechnoExchange(it) },
                expression.biosphere.map { reduceBioExchange(it) },
            )

            is EProcessRef -> processEnvironment[expression.name]?.let { reduceLcaProcessExpression(it) } ?: expression
        }
    }

    private fun reduceImpact(expression: EImpact) = EImpact(
        quantityExpressionReducer.reduce(expression.quantity),
        reduceIndicatorExpression(expression.indicator),
    )

    private fun reduceBioExchange(expression: EBioExchange) = EBioExchange(
        quantityExpressionReducer.reduce(expression.quantity),
        reduceSubstanceExpression(expression.substance),
    )

    private fun reduceTechnoExchange(expression: ETechnoExchange): ETechnoExchange {
        return ETechnoExchange(
            quantityExpressionReducer.reduce(expression.quantity),
            reduceProductExpression(expression.product),
        )
    }

    private fun reduceUnconstrainedProductExpression(expression: LcaUnconstrainedProductExpression): LcaUnconstrainedProductExpression {
        return when (expression) {
            is EProduct -> EProduct(expression.name, unitExpressionReducer.reduce(expression.referenceUnit))
            is EProductRef -> productEnvironment[expression.name]?.let { reduceUnconstrainedProductExpression(it) }
                ?: expression
        }
    }

    private fun reduceProductExpression(expression: LcaProductExpression): LcaProductExpression {
        return when (expression) {
            is EConstrainedProduct -> EConstrainedProduct(
                reduceUnconstrainedProductExpression(expression.product),
                expression.constraint.reduceWith(quantityExpressionReducer),
            )

            is EProduct -> reduceUnconstrainedProductExpression(expression)
            is EProductRef -> reduceUnconstrainedProductExpression(expression)
        }
    }

    private fun reduceSubstanceExpression(expression: LcaSubstanceExpression): LcaSubstanceExpression {
        return when (expression) {
            is ESubstance -> ESubstance(
                expression.name,
                expression.compartment,
                expression.subcompartment,
                unitExpressionReducer.reduce(expression.referenceUnit),
            )

            is ESubstanceRef -> substanceEnvironment[expression.name]?.let { reduceSubstanceExpression(it) }
                ?: expression
        }
    }

    private fun reduceIndicatorExpression(expression: LcaIndicatorExpression): LcaIndicatorExpression {
        return when (expression) {
            is EIndicator -> EIndicator(
                expression.name,
                unitExpressionReducer.reduce(expression.referenceUnit),
            )

            is EIndicatorRef -> indicatorEnvironment[expression.name]?.let { reduceIndicatorExpression(it) }
                ?: expression
        }
    }
}
