package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.expression.*

class LcaExpressionReducer(
    dataRegister: Register<DataExpression> = Register.empty(),
) : Reducer<LcaExpression> {
    private val dataExpressionReducer = DataExpressionReducer(dataRegister)

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
            expression.impacts.map(::reduceImpact),
        )
    }


    private fun reduceProcess(expression: EProcess): EProcess {
        return EProcess(
            expression.name,
            expression.labels,
            expression.products.map(::reduceTechnoExchange),
            expression.inputs.map(::reduceTechnoExchange),
            expression.biosphere.map(::reduceBioExchange),
            expression.impacts.map(::reduceImpact)
        )
    }

    private fun reduceImpact(expression: EImpact) = EImpact(
        dataExpressionReducer.reduce(expression.quantity),
        reduceIndicatorSpec(expression.indicator),
    )

    private fun reduceBioExchange(expression: EBioExchange) = EBioExchange(
        dataExpressionReducer.reduce(expression.quantity),
        reduceSubstanceSpec(expression.substance),
    )

    private fun reduceTechnoExchange(expression: ETechnoExchange): ETechnoExchange {
        return ETechnoExchange(
            dataExpressionReducer.reduce(expression.quantity),
            reduceProductSpec(expression.product),
            expression.allocation?.let { dataExpressionReducer.reduce(it) }
        )
    }

    private fun reduceProductSpec(expression: EProductSpec): EProductSpec {
        return EProductSpec(
            expression.name,
            expression.referenceUnit?.let { dataExpressionReducer.reduce(it) },
            expression.fromProcess?.let { ref ->
                ref.copy(
                    matchLabels = MatchLabels(ref.matchLabels.elements.mapValues { dataExpressionReducer.reduce(it.value) }),
                    arguments = ref.arguments.mapValues { dataExpressionReducer.reduce(it.value) }
                )
            },
        )
    }

    private fun reduceSubstanceSpec(expression: ESubstanceSpec): ESubstanceSpec {
        return ESubstanceSpec(
            expression.name,
            expression.displayName,
            expression.type,
            expression.compartment,
            expression.subCompartment,
            expression.referenceUnit?.let { dataExpressionReducer.reduce(it) },
        )
    }

    private fun reduceIndicatorSpec(expression: EIndicatorSpec): EIndicatorSpec {
        return EIndicatorSpec(
            expression.name,
            expression.referenceUnit?.let { dataExpressionReducer.reduce(it) },
        )
    }
}
