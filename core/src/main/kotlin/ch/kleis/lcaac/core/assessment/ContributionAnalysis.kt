package ch.kleis.lcaac.core.assessment

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.core.matrix.ImpactFactorMatrix
import ch.kleis.lcaac.core.matrix.IndexedCollection
import ch.kleis.lcaac.core.matrix.IntensityMatrix

class ContributionAnalysis<Q, M>(
    val entryPoint: ProcessValue<Q>,
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

    fun getImpactFactors(): ImpactFactorMatrix<Q, M> {
        return impactFactors
    }

    fun findOwnerOf(product: ProductValue<Q>): ProcessValue<Q>? {
        return allocatedSystem.productToProcessMap[product]
    }

    fun getUnitaryImpacts(target: MatrixColumnIndex<Q>): Map<MatrixColumnIndex<Q>, QuantityValue<Q>> {
        return impactFactors.unitaryImpacts(target)
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

    fun getProducts(): List<ProductValue<Q>> {
        return (getObservablePorts().getElements() + getControllablePorts().getElements()).filterIsInstance<ProductValue<Q>>()
    }

    fun getUnresolvedProducts(): List<ProductValue<Q>> {
        return getControllablePorts().getElements().filterIsInstance<ProductValue<Q>>()
    }

    fun getSubstances(): List<SubstanceValue<Q>> {
        return (getObservablePorts().getElements() + getControllablePorts().getElements()).filterIsInstance<SubstanceValue<Q>>()
    }

    fun getNonCharacterizedSubstances(): List<SubstanceValue<Q>> {
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
        return when {
            getObservablePorts().contains(port) -> supplyOfObservablePort(port)
            getControllablePorts().contains(port) -> supplyOfControllablePort(port)
            else -> throw EvaluatorException("unknown $port")
        }
    }

    fun allocatedSupplyOf(port: MatrixColumnIndex<Q>, product: ProductValue<Q>): QuantityValue<Q> {
        with(quantityOps) {
            val exchange = entryPoint.products.firstOrNull { it.product == product } ?: throw EvaluatorException("$product does not belong to the demand")
            val allocation = exchange.allocation
                ?.let { pure(it.toDouble()) }
                ?: pure(1.0)
            return allocation * supplyOf(port)
        }
    }

    private fun supplyOfObservablePort(port: MatrixColumnIndex<Q>): QuantityValue<Q> {
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

    private fun supplyOfControllablePort(port: MatrixColumnIndex<Q>): QuantityValue<Q> {
        with(quantityOps) {
            val entryPointProducts = entryPoint.products.map { it.product }
            return entryPointProducts
                .map { getPortContribution(it, port) }
                .reduce { acc, value -> acc + value }
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

