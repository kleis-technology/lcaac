package ch.kleis.lcaplugin.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*

object CompleteTerminals {
    private val everyInputExchange =
        EProcessFinal.expression.inputs compose
            Every.list()

    fun apply(expression: EProcessFinal): EProcessFinal =
        expression
            .completeInputs()
            .completeSubstances()
            .completeProcessIndicators()

    fun apply(expression: ESubstanceCharacterization): ESubstanceCharacterization =
        expression.completeSubstanceIndicators()

    private fun EProcessFinal.completeInputs(): EProcessFinal {
        return everyInputExchange
            .modify(this) { exchange ->
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

    private fun EProcessFinal.completeSubstances(): EProcessFinal {
        return (EProcessFinal.expression.biosphere compose Every.list())
            .modify(this) { exchange ->
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

    private fun completeIndicators(impacts: Collection<EImpact>): List<EImpact> =
        impacts.map { impactExchange ->
            val quantityExpression = impactExchange.quantity
            val referenceUnit = when {
                quantityExpression is EUnitLiteral ->
                    EQuantityScale(1.0, quantityExpression)

                quantityExpression is EQuantityScale && quantityExpression.base is EUnitLiteral ->
                    EQuantityScale(1.0, quantityExpression.base)

                else -> throw EvaluatorException("quantity $quantityExpression is not reduced")
            }

            EImpact.indicator
                .modify(impactExchange) {
                    EIndicatorSpec(it.name, referenceUnit)
                }
        }

    private fun EProcessFinal.completeProcessIndicators(): EProcessFinal =
        this.copy(
            expression = this.expression.copy(
                impacts = completeIndicators(this.expression.impacts)
            )
        )

    private fun ESubstanceCharacterization.completeSubstanceIndicators(): ESubstanceCharacterization =
        this.copy(impacts = completeIndicators(this.impacts))
}
