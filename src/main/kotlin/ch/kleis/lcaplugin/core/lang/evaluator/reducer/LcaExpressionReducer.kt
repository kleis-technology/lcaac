package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.expression.*

class LcaExpressionReducer(
    indicatorRegister: Register<EIndicator> = Register.empty(),
    quantityRegister: Register<QuantityExpression> = Register.empty(),
    unitRegister: Register<UnitExpression> = Register.empty(),
) : Reducer<LcaExpression> {
    private val indicatorRegister = Register(indicatorRegister)
    private val quantityExpressionReducer = QuantityExpressionReducer(quantityRegister, unitRegister)

    override fun reduce(expression: LcaExpression): LcaExpression {
        return when (expression) {
            is EProcess -> reduceProcess(expression)

            is EImpact -> reduceImpact(expression)

            is ETechnoExchange -> reduceTechnoExchange(expression)
            is EBioExchange -> reduceBioExchange(expression)

            is EIndicator -> reduceIndicatorExpression(expression)
            is EIndicatorRef -> reduceIndicatorExpression(expression)

            is ESubstanceSpec -> reduceSubstanceSpec(expression)

            is EProductSpec -> reduceProductSpec(expression)

            is ESubstanceCharacterization -> reduceSubstanceCharacterization(expression)
        }
    }

    fun reduceSubstanceCharacterization(expression: ESubstanceCharacterization): ESubstanceCharacterization {
        return ESubstanceCharacterization(
            reduceBioExchange(expression.referenceExchange),
            expression.impacts.map { reduceImpact(it) },
        )
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
        reduceSubstanceSpec(expression.substance),
    )

    private fun reduceTechnoExchange(expression: ETechnoExchange): ETechnoExchange {
        return ETechnoExchange(
            quantityExpressionReducer.reduce(expression.quantity),
            reduceProductSpec(expression.product),
            quantityExpressionReducer.reduce(expression.allocation)
        )
    }

    private fun reduceProductSpec(expression: EProductSpec): EProductSpec {
        return EProductSpec(
            expression.name,
            expression.referenceUnit?.let { quantityExpressionReducer.reduceUnit(it) },
            expression.fromProcessRef?.reduceWith(quantityExpressionReducer),
        )
    }

    private fun reduceSubstanceSpec(expression: ESubstanceSpec): ESubstanceSpec {
        return ESubstanceSpec(
            expression.name,
            expression.displayName,
            expression.type,
            expression.compartment,
            expression.subcompartment,
            expression.referenceUnit?.let { quantityExpressionReducer.reduceUnit(it) },
        )
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
