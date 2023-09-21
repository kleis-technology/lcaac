package ch.kleis.lcaac.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.QuantityOperations

class CompleteTerminals<Q>(
    private val ops: QuantityOperations<Q>
) {
    private val everyInputExchange =
        EProcessFinal.expression<Q>().inputs() compose
                Every.list()

    fun apply(expression: EProcessFinal<Q>): EProcessFinal<Q> =
        expression
            .completeInputs()
            .completeSubstances()
            .completeProcessIndicators()

    fun apply(expression: ESubstanceCharacterization<Q>): ESubstanceCharacterization<Q> =
        expression.completeSubstanceIndicators()

    private fun EProcessFinal<Q>.completeInputs(): EProcessFinal<Q> {
        return everyInputExchange
            .modify(this) { exchange ->
                val referenceUnit = exchangeReferenceUnit(exchange)

                ETechnoExchange.product<Q>()
                    .modify(exchange) {
                        it.copy(referenceUnit = referenceUnit)
                    }
            }
    }

    private fun EProcessFinal<Q>.completeSubstances(): EProcessFinal<Q> {
        return (EProcessFinal.expression<Q>().biosphere() compose Every.list())
            .modify(this) { exchange ->
                val referenceUnit = exchangeReferenceUnit(exchange)

                EBioExchange.substance<Q>()
                    .modify(exchange) {
                        if (it.referenceUnit == null) {
                            it.copy(referenceUnit = referenceUnit)
                        } else it
                    }
            }
    }

    private fun completeIndicators(impacts: Collection<EImpact<Q>>): List<EImpact<Q>> =
        impacts.map { exchange ->
            val referenceUnit = exchangeReferenceUnit(exchange)

            EImpact.indicator<Q>()
                .modify(exchange) {
                    EIndicatorSpec(it.name, referenceUnit)
                }
        }

    private fun EProcessFinal<Q>.completeProcessIndicators(): EProcessFinal<Q> =
        this.copy(
            expression = this.expression.copy(
                impacts = completeIndicators(this.expression.impacts)
            )
        )

    private fun ESubstanceCharacterization<Q>.completeSubstanceIndicators(): ESubstanceCharacterization<Q> =
        this.copy(impacts = completeIndicators(this.impacts))

    private fun exchangeReferenceUnit(exchange: LcaExchangeExpression<Q>): EQuantityScale<Q> {
        val quantityExpression = exchange.quantity
        val referenceUnit = when {
            quantityExpression is EUnitLiteral ->
                EQuantityScale(ops.pure(1.0), quantityExpression)

            quantityExpression is EQuantityScale && quantityExpression.base is EUnitLiteral ->
                EQuantityScale(ops.pure(1.0), quantityExpression.base)

            else -> throw EvaluatorException("quantity $quantityExpression is not reduced")
        }
        return referenceUnit
    }
}

