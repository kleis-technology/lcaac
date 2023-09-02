package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.core.math.basic.BasicMatrix
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import ch.kleis.lcaplugin.core.matrix.ImpactFactorMatrix
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.IntensityMatrix

class ContributionAnalysis(
    private val impactFactors: ImpactFactorMatrix<BasicNumber, BasicMatrix>,
    private val intensity: IntensityMatrix<BasicNumber, BasicMatrix>,
    private val allocatedSystem: SystemValue<BasicNumber>,
) {
    fun getImpactFactors(): ImpactFactorMatrix<BasicNumber, BasicMatrix> {
        return impactFactors
    }

    fun findOwnerOf(product: ProductValue<BasicNumber>): ProcessValue<BasicNumber>? {
        return allocatedSystem.productToProcessMap[product]
    }

    @Deprecated("remove me")
    fun getImpactFactorsOf(target: MatrixColumnIndex<BasicNumber>): Map<MatrixColumnIndex<BasicNumber>, QuantityValue<BasicNumber>> {
        return impactFactors.rowAsMap(target)
    }

    @Deprecated("remove me")
    fun getNumberOfImpactFactors(): Int {
        return impactFactors.nbCells()
    }

    fun getObservablePorts(): IndexedCollection<MatrixColumnIndex<BasicNumber>> {
        return impactFactors.observablePorts
    }

    fun getControllablePorts(): IndexedCollection<MatrixColumnIndex<BasicNumber>> {
        return impactFactors.controllablePorts
    }

    fun supplyOf(port: MatrixColumnIndex<BasicNumber>): QuantityValue<BasicNumber> {
        with(QuantityValueOperations(BasicOperations)) {
            return when (port) {
                is ProductValue<BasicNumber> -> {
                    val process = allocatedSystem.productToProcessMap[port] ?: throw EvaluatorException("unknown $port")
                    val intensity = intensity.intensityOf(process)
                    val exchangeQuantity =
                        process.outputExchangesByProduct[port]?.quantity ?: throw EvaluatorException("unknown $port")
                    intensity * exchangeQuantity
                }
                is SubstanceValue<BasicNumber> -> {
                    val substanceCharacterization = allocatedSystem.substanceToSubstanceCharacterizationMap[port] ?: throw EvaluatorException("unknown $port")
                    val intensity = intensity.intensityOf(substanceCharacterization)
                    val exchangeQuantity = substanceCharacterization.referenceExchange.quantity
                    intensity * exchangeQuantity
                }
                else -> throw IllegalStateException()
            }
        }
    }

    fun getPortContribution(
        port: MatrixColumnIndex<BasicNumber>,
        indicator: MatrixColumnIndex<BasicNumber>,
    ): QuantityValue<BasicNumber> {
        val supply = supplyOf(port)
        val factor = impactFactors.characterizationFactor(port, indicator)
        with(QuantityValueOperations(BasicOperations)) {
            return supply * factor
        }
    }

    fun getExchangeContribution(
        port: MatrixColumnIndex<BasicNumber>,
        exchange: ExchangeValue<BasicNumber>,
        indicator: MatrixColumnIndex<BasicNumber>,
    ): QuantityValue<BasicNumber> {
        val process = allocatedSystem.productToProcessMap[port] ?: throw EvaluatorException("unknown $port")
        val intensity = intensity.intensityOf(process)
        val factor = impactFactors.characterizationFactor(exchange.port(), indicator)
        with(QuantityValueOperations(BasicOperations)) {
            return intensity * exchange.quantity() * factor
        }
    }
}
