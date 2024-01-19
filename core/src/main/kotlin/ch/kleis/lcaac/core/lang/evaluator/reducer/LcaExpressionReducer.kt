package ch.kleis.lcaac.core.lang.evaluator.reducer

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.register.DataRegister
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.math.QuantityOperations

class LcaExpressionReducer<Q>(
    dataRegister: DataRegister<Q> = DataRegister.empty(),
    dataSourceRegister: DataSourceRegister<Q> = DataSourceRegister.empty(),
    ops: QuantityOperations<Q>,
    sourceOps: DataSourceOperations<Q>,
) {
    private val dataExpressionReducer = DataExpressionReducer(dataRegister, dataSourceRegister, ops, sourceOps)

    fun reduce(expression: LcaExpression<Q>): LcaExpression<Q> {
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

    fun reduceSubstanceCharacterization(expression: ESubstanceCharacterization<Q>): ESubstanceCharacterization<Q> {
        return ESubstanceCharacterization(
            reduceBioExchange(expression.referenceExchange),
            expression.impacts.map(::reduceImpact),
        )
    }


    private fun reduceProcess(expression: EProcess<Q>): EProcess<Q> {
        return EProcess(
            expression.name,
            expression.labels,
            expression.products.map(::reduceTechnoExchange),
            expression.inputs.map(::reduceTechnoExchange),
            expression.biosphere.map(::reduceBioExchange),
            expression.impacts.map(::reduceImpact)
        )
    }

    private fun reduceImpact(expression: EImpact<Q>) = EImpact(
        dataExpressionReducer.reduce(expression.quantity),
        reduceIndicatorSpec(expression.indicator),
    )

    private fun reduceBioExchange(expression: EBioExchange<Q>) = EBioExchange(
        dataExpressionReducer.reduce(expression.quantity),
        reduceSubstanceSpec(expression.substance),
    )

    private fun reduceTechnoExchange(expression: ETechnoExchange<Q>): ETechnoExchange<Q> {
        return ETechnoExchange(
            dataExpressionReducer.reduce(expression.quantity),
            reduceProductSpec(expression.product),
            expression.allocation?.let { dataExpressionReducer.reduce(it) }
        )
    }

    private fun reduceProductSpec(expression: EProductSpec<Q>): EProductSpec<Q> {
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

    private fun reduceSubstanceSpec(expression: ESubstanceSpec<Q>): ESubstanceSpec<Q> {
        return ESubstanceSpec(
            expression.name,
            expression.displayName,
            expression.type,
            expression.compartment,
            expression.subCompartment,
            expression.referenceUnit?.let { dataExpressionReducer.reduce(it) },
        )
    }

    private fun reduceIndicatorSpec(expression: EIndicatorSpec<Q>): EIndicatorSpec<Q> {
        return EIndicatorSpec(
            expression.name,
            expression.referenceUnit?.let { dataExpressionReducer.reduce(it) },
        )
    }
}
