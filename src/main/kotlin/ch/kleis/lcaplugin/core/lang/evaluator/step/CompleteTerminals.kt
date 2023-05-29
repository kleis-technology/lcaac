package ch.kleis.lcaplugin.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*

class CompleteTerminals {
    private val everyInputExchange =
        EProcessFinal.expression.inputs compose
            Every.list()

    fun apply(expression: EProcessFinal): EProcessFinal {
        return completeSubstances(completeInputs(expression))
    }

    fun apply(expression: ESubstanceCharacterization): ESubstanceCharacterization {
        return completeIndicators(expression)
    }

    private fun completeInputs(reduced: EProcessFinal): EProcessFinal {
        return everyInputExchange
            .modify(reduced) { exchange ->
                val quantityExpression = exchange.quantity
                val referenceUnit = when {
                    quantityExpression is EUnitLiteral ->
                        EQuantityScale(1.0, quantityExpression)

                    quantityExpression is EQuantityScale && quantityExpression.base is EUnitLiteral ->
                        EQuantityScale(1.0, quantityExpression.base)

                    else -> throw EvaluatorException("quantity $quantityExpression is not reduced")
                }

                ETechnoExchange.product
                    .modify(exchange) {
                        it.copy(referenceUnit = referenceUnit)
                    }
            }
    }

    private fun completeSubstances(reduced: EProcessFinal): EProcessFinal {
        return (EProcessFinal.expression.biosphere compose Every.list())
            .modify(reduced) { exchange ->
                val quantityExpression = exchange.quantity
                val referenceUnit = when {
                    quantityExpression is EUnitLiteral ->
                        EQuantityScale(1.0, quantityExpression)

                    quantityExpression is EQuantityScale && quantityExpression.base is EUnitLiteral ->
                        EQuantityScale(1.0, quantityExpression.base)

                    else -> throw EvaluatorException("quantity $quantityExpression is not reduced")
                }

                EBioExchange.substance
                    .modify(exchange) {
                        if (it.referenceUnit == null) {
                            it.copy(referenceUnit = referenceUnit)
                        } else it
                    }
            }
    }

    private fun completeIndicators(reduced: ESubstanceCharacterization): ESubstanceCharacterization {
        return (ESubstanceCharacterization.impacts compose Every.list())
            .modify(reduced) { exchange ->
                val quantityExpression = exchange.quantity
                val referenceUnit = when {
                    quantityExpression is EUnitLiteral ->
                        EQuantityScale(1.0, quantityExpression)

                    quantityExpression is EQuantityScale && quantityExpression.base is EUnitLiteral ->
                        EQuantityScale(1.0, quantityExpression.base)

                    else -> throw EvaluatorException("quantity $quantityExpression is not reduced")
                }

                EImpact.indicator
                    .modify(exchange) {
                        EIndicatorSpec(it.name, referenceUnit)
                    }
            }
    }
}
