package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.ParameterName
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.core.math.dual.DualMatrix
import ch.kleis.lcaplugin.core.math.dual.DualNumber
import ch.kleis.lcaplugin.core.math.dual.DualOperations
import ch.kleis.lcaplugin.core.matrix.ImpactFactorMatrix
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.IntensityMatrix
import ch.kleis.lcaplugin.core.matrix.ParameterVector
import org.jetbrains.kotlinx.multik.ndarray.data.get

class SensitivityAnalysis(
    private val entryPoint: ProcessValue<DualNumber>,
    private val impactFactors: ImpactFactorMatrix<DualNumber, DualMatrix>,
    private val intensity: IntensityMatrix<DualNumber, DualMatrix>,
    private val allocatedSystem: SystemValue<DualNumber>,
    private val parameters: ParameterVector<DualNumber>,
) {
    fun getEntryPointProducts(): List<ProductValue<DualNumber>> {
        return entryPoint.products.map { it.product }
    }

    fun getControllablePorts(): IndexedCollection<MatrixColumnIndex<DualNumber>> {
        return impactFactors.controllablePorts
    }

    fun getObservablePorts(): IndexedCollection<MatrixColumnIndex<DualNumber>> {
        return impactFactors.observablePorts
    }

    fun getParameters(): ParameterVector<DualNumber> {
        return parameters
    }

    fun getRelativeSensibility(
        target: MatrixColumnIndex<DualNumber>,
        indicator: MatrixColumnIndex<DualNumber>,
        parameter: ParameterName,
    ): Double {
        val parameterIndex = parameters.indexOf(parameter)
        val impactFactor = impactFactors.unitaryImpact(target, indicator).amount
        val base = impactFactor.zeroth
        val absoluteSensibility = impactFactor.first[parameterIndex]
        return absoluteSensibility / base
    }

    fun getPortContribution(
        port: MatrixColumnIndex<DualNumber>,
        indicator: MatrixColumnIndex<DualNumber>,
    ): QuantityValue<DualNumber> {
        val supply = supplyOf(port)
        val factor = impactFactors.characterizationFactor(port, indicator)
        with(QuantityValueOperations(DualOperations(parameters.size()))) {
            return supply * factor
        }
    }

    private fun supplyOf(port: MatrixColumnIndex<DualNumber>): QuantityValue<DualNumber> {
        with(QuantityValueOperations(DualOperations(parameters.size()))) {
            return when (port) {
                is ProductValue<DualNumber> -> {
                    val process = allocatedSystem.productToProcessMap[port] ?: throw EvaluatorException("unknown $port")
                    val intensity = intensity.intensityOf(process)
                    val exchangeQuantity = process.outputExchangesByProduct[port]?.quantity ?: throw EvaluatorException("unknown $port")
                    intensity * exchangeQuantity
                }

                is SubstanceValue<DualNumber> -> {
                    val substanceCharacterization = allocatedSystem.substanceToSubstanceCharacterizationMap[port]
                        ?: throw EvaluatorException("unknown $port")
                    val intensity = intensity.intensityOf(substanceCharacterization)
                    val exchangeQuantity = substanceCharacterization.referenceExchange.quantity
                    intensity * exchangeQuantity
                }

                else -> throw IllegalStateException()
            }
        }
    }

}
