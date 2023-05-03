package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.expression.*

class LcaExpressionReducer(
        quantityRegister: Register<QuantityExpression> = Register.empty(),
        unitRegister: Register<UnitExpression> = Register.empty(),
) : Reducer<LcaExpression> {
    private val quantityExpressionReducer = QuantityExpressionReducer(quantityRegister, unitRegister)

    override fun reduce(expression: LcaExpression): LcaExpression {
        return when (expression) {
            is EProcess -> reduceProcess(expression)
            is ESubstanceCharacterization -> reduceSubstanceCharacterization(expression)

            is EImpact -> reduceImpact(expression)
            is ETechnoExchange -> reduceTechnoExchange(expression)
            is EBioExchange -> reduceBioExchange(expression)

            is EIndicatorSpec -> reduceIndicatorSpec(expression)
            is ESubstanceSpec -> reduceSubstanceSpec(expression)
            is EProductSpec -> reduceProductSpec(expression)
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
            reduceIndicatorSpec(expression.indicator),
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
                expression.subCompartment,
                expression.referenceUnit?.let { quantityExpressionReducer.reduceUnit(it) },
        )
    }

    private fun reduceIndicatorSpec(expression: EIndicatorSpec): EIndicatorSpec {
        return EIndicatorSpec(
                expression.name,
                expression.referenceUnit?.let { quantityExpressionReducer.reduceUnit(it) },
        )
    }
}
