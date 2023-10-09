package ch.kleis.lcaac.core.assessment

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.core.matrix.ImpactFactorMatrix
import ch.kleis.lcaac.core.matrix.IndexedCollection
import ch.kleis.lcaac.core.matrix.IntensityMatrix

class ContributionAnalysis<Q, M>(
    private val entryPoint: ProcessValue<Q>,
    private val impactFactors: ImpactFactorMatrix<Q, M>,
    private val intensity: IntensityMatrix<Q, M>,
    private val allocatedSystem: SystemValue<Q>,
    ops: QuantityOperations<Q>,
) {
    private val quantityOps = QuantityValueOperations(ops)
    private val allPorts = IndexByShortName(
        impactFactors.observablePorts.getElements()
            + impactFactors.controllablePorts.getElements()
    )

    fun getEntryPoint(): ProcessValue<Q> {
        return entryPoint
    }

    fun getImpactFactors(): ImpactFactorMatrix<Q, M> {
        return impactFactors
    }

    fun findOwnerOf(product: ProductValue<Q>): ProcessValue<Q>? {
        return allocatedSystem.productToProcessMap[product]
    }

    fun getUnitaryImpacts(target: MatrixColumnIndex<Q>): Map<MatrixColumnIndex<Q>, QuantityValue<Q>> {
        return impactFactors.unitaryImpacts(target)
    }

    fun getNbCells(): Int {
        return impactFactors.nbCells()
    }

    fun getObservablePorts(): IndexedCollection<MatrixColumnIndex<Q>> {
        return impactFactors.observablePorts
    }

    fun getControllablePorts(): IndexedCollection<MatrixColumnIndex<Q>> {
        return impactFactors.controllablePorts
    }

    fun getIndicators(): List<IndicatorValue<Q>> {
        return getControllablePorts().getElements().filterIsInstance<IndicatorValue<Q>>()
    }

    fun getObservableProducts(): List<ProductValue<Q>> {
        return getObservablePorts().getElements().filterIsInstance<ProductValue<Q>>()
    }

    fun getControllableProducts(): List<ProductValue<Q>> {
        return getControllablePorts().getElements().filterIsInstance<ProductValue<Q>>()
    }

    fun getObservableSubstances(): List<SubstanceValue<Q>> {
        return getObservablePorts().getElements().filterIsInstance<SubstanceValue<Q>>()
    }

    fun getControllableSubstances(): List<SubstanceValue<Q>> {
        return getControllablePorts().getElements().filterIsInstance<SubstanceValue<Q>>()
    }

    fun isControllable(port: MatrixColumnIndex<Q>): Boolean {
        return getControllablePorts().contains(port)
    }

    fun findAllPortsByShortName(shortName: String): List<MatrixColumnIndex<Q>> {
        return allPorts.findByShortName(shortName)
    }

    fun getExchangeContribution(
        port: MatrixColumnIndex<Q>,
        exchange: ExchangeValue<Q>,
        indicator: MatrixColumnIndex<Q>,
    ): QuantityValue<Q> {
        val process = allocatedSystem.productToProcessMap[port] ?: throw EvaluatorException("unknown $port")
        val intensity = intensity.intensityOf(process)
        val factor = impactFactors.characterizationFactor(exchange.port(), indicator)
        with(quantityOps) {
            return intensity * exchange.quantity() * factor
        }
    }


    fun supplyOf(port: MatrixColumnIndex<Q>): QuantityValue<Q> {
        with(quantityOps) {
            return when (port) {
                is ProductValue<Q> -> {
                    val process = allocatedSystem.productToProcessMap[port] ?: throw EvaluatorException("unknown $port")
                    val intensity = intensity.intensityOf(process)
                    val exchangeQuantity =
                        process.outputExchangesByProduct[port]?.quantity ?: throw EvaluatorException("unknown $port")
                    intensity * exchangeQuantity
                }

                is SubstanceValue<Q> -> {
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

    fun getPortContribution(
        port: MatrixColumnIndex<Q>,
        indicator: MatrixColumnIndex<Q>,
    ): QuantityValue<Q> {
        val supply = supplyOf(port)
        val factor = impactFactors.characterizationFactor(port, indicator)
        return with(quantityOps) {
            supply * factor
        }
    }

    private class IndexByShortName<Q>(
        private val elements: List<MatrixColumnIndex<Q>>,
    ) {
        private val byShortName = HashMap<String, List<Int>>()

        init {
            elements.forEachIndexed { index, port ->
                val shortName = port.getShortName()
                byShortName[shortName] = byShortName[shortName]?.plus(index)
                    ?: listOf(index)
            }
        }

        fun findByShortName(shortName: String): List<MatrixColumnIndex<Q>> {
            val indices = byShortName[shortName] ?: return emptyList()
            return indices.map { elements[it] }
        }
    }
}

