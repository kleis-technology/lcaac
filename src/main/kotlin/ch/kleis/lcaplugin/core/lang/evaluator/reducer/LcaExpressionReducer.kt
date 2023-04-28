package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.expression.*

class LcaExpressionReducer(
    productRegister: Register<LcaUnconstrainedProductExpression> = Register.empty(),
    substanceRegister: Register<LcaSubstanceExpression> = Register.empty(),
    indicatorRegister: Register<LcaIndicatorExpression> = Register.empty(),
    quantityRegister: Register<QuantityExpression> = Register.empty(),
    unitRegister: Register<UnitExpression> = Register.empty(),
    substanceCharacterizationRegister: Register<LcaSubstanceCharacterizationExpression> = Register.empty(),
) : Reducer<LcaExpression> {
    private val productRegister = Register(productRegister)
    private val substanceRegister = Register(substanceRegister)
    private val indicatorRegister = Register(indicatorRegister)
    private val substanceCharacterizationRegister = Register(substanceCharacterizationRegister)

    private val quantityExpressionReducer = QuantityExpressionReducer(quantityRegister, unitRegister)

    override fun reduce(expression: LcaExpression): LcaExpression {
        return when (expression) {
            is EProcess -> reduceProcess(expression)

            is EImpact -> reduceImpact(expression)

            is ETechnoExchange -> reduceTechnoExchange(expression)
            is EBioExchange -> reduceBioExchange(expression)

            is EIndicator -> reduceIndicatorExpression(expression)
            is EIndicatorRef -> reduceIndicatorExpression(expression)

            is ESubstance -> reduceSubstanceExpression(expression)
            is ESubstanceRef -> reduceSubstanceExpression(expression)

            is EConstrainedProduct -> EConstrainedProduct(
                reduceUnconstrainedProductExpression(expression.product),
                expression.constraint.reduceWith(quantityExpressionReducer),
            )

            is ESubstanceCharacterization -> reduceSubstanceCharacterization(expression)
        }
    }

    fun reduceSubstanceCharacterization(expression: LcaSubstanceCharacterizationExpression): LcaSubstanceCharacterizationExpression {
        return when (expression) {
            is ESubstanceCharacterization -> ESubstanceCharacterization(
                reduceBioExchange(expression.referenceExchange),
                expression.impacts.map { reduceImpact(it) },
            )
        }
    }


    private fun reduceProcess(expression: EProcess): EProcess {
        return EProcess(
            expression.name,
            expression.products.map { reduceTechnoExchange(it) },
            expression.inputs.map { reduceTechnoExchange(it) },
            expression.biosphere.map { reduceBioExchange(it) },
        )
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
            reduceConstrainedProduct(expression.product),
            quantityExpressionReducer.reduce(expression.allocation)
        )
    }

    private fun reduceUnconstrainedProductExpression(expression: LcaUnconstrainedProductExpression): LcaUnconstrainedProductExpression {
        return when (expression) {
            is EProduct -> EProduct(expression.name, quantityExpressionReducer.reduceUnit(expression.referenceUnit))
            is EProductRef -> productRegister[expression.name]?.let { reduceUnconstrainedProductExpression(it) }
                ?: expression
        }
    }

    private fun reduceConstrainedProduct(expression: EConstrainedProduct): EConstrainedProduct {
        return EConstrainedProduct(
            reduceUnconstrainedProductExpression(expression.product),
            expression.constraint.reduceWith(quantityExpressionReducer),
        )
    }

    private fun reduceSubstanceExpression(expression: LcaSubstanceExpression): LcaSubstanceExpression {
        return when (expression) {
            is ESubstance -> ESubstance(
                expression.name,
                expression.displayName,
                expression.type,
                expression.compartment,
                expression.subcompartment,
                quantityExpressionReducer.reduceUnit(expression.referenceUnit),
            )

            is ESubstanceRef -> substanceRegister[expression.name]?.let { reduceSubstanceExpression(it) }
                ?: expression
        }
    }

    private fun reduceIndicatorExpression(expression: LcaIndicatorExpression): LcaIndicatorExpression {
        return when (expression) {
            is EIndicator -> EIndicator(
                expression.name,
                quantityExpressionReducer.reduceUnit(expression.referenceUnit),
            )

            is EIndicatorRef -> indicatorRegister[expression.name]?.let { reduceIndicatorExpression(it) }
                ?: expression
        }
    }
}
