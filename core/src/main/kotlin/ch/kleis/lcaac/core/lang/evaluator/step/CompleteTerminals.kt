package ch.kleis.lcaac.core.lang.evaluator.step

import arrow.optics.Every
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.QuantityOperations

class CompleteTerminals<Q>(
    private val ops: QuantityOperations<Q>
) {
    private val everyInputExchange =
        EProcess.inputs<Q>() compose
                Every.list()

    fun apply(expression: EProcess<Q>): EProcess<Q> =
        expression
            .completeInputs()
            .completeSubstances()
            .completeProcessIndicators()

    fun apply(expression: ESubstanceCharacterization<Q>): ESubstanceCharacterization<Q> =
        expression.completeSubstanceIndicators()

    private fun EProcess<Q>.completeInputs(): EProcess<Q> {
        return everyInputExchange
            .modify(this) { exchange ->
                val referenceUnit = exchangeReferenceUnit(exchange)

                ETechnoExchange.product<Q>()
                    .modify(exchange) {
                        it.copy(referenceUnit = referenceUnit)
                    }
            }
    }

    private fun EProcess<Q>.completeSubstances(): EProcess<Q> {
        return (EProcess.biosphere<Q>() compose Every.list())
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

    private fun EProcess<Q>.completeProcessIndicators(): EProcess<Q> =
        this.copy(
                impacts = completeIndicators(this.impacts)
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

