package ch.kleis.lcaac.core.allocation

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.QuantityOperations

/** Why the allocation of a process is invalid, and the index of the product concerned, if any. */
data class AllocationProblem(
    val message: String,
    val productIndex: Int? = null,
)

/**
 * Splits a process with several products into one process per product. Each product gets a share
 * of the inputs, emissions and impacts: its allocation weight divided by the total weight of the
 * products. The weights can have any unit, as long as they all have the same dimension.
 */
class Allocation<Q>(
    private val ops: QuantityOperations<Q>,
) {
    private val quantityOps = QuantityValueOperations(ops)

    fun apply(system: SystemValue<Q>): SystemValue<Q> {
        val processes = system.processes.flatMap { processValue ->
            processValue.products.map { allocateProduct(it, processValue) }
        }.toMutableSet()
        return SystemValue(processes, system.substanceCharacterizations)
    }

    private fun allocateProduct(
        technoExchangeValue: TechnoExchangeValue<Q>,
        processValue: ProcessValue<Q>
    ): ProcessValue<Q> {
        val ratio = ratioOf(technoExchangeValue, processValue)
        return ProcessValue(
            name = processValue.name,
            labels = processValue.labels,
            products = listOf(technoExchangeValue.copy(allocation = technoExchangeValue.allocation)),
            inputs = processValue.inputs.map(applyAllocationToInput(ratio)),
            biosphere = processValue.biosphere.map(applyAllocationToBioExchange(ratio)),
            impacts = processValue.impacts.map(applyAllocationToImpact(ratio)),
        )
    }

    /** The share of [product] in [process]. A process with a single product needs no allocation. */
    fun ratioOf(product: TechnoExchangeValue<Q>, process: ProcessValue<Q>): Double {
        if (process.products.size <= 1) return 1.0
        problemOf(process)?.let { throw EvaluatorException(it.message) }
        return weightOf(product) / process.products.sumOf { weightOf(it) }
    }

    /** Why the allocation of [process] is invalid, or null if it is valid. */
    fun problemOf(process: ProcessValue<Q>): AllocationProblem? {
        val products = process.products
        if (products.size <= 1) return null

        val missing = products.indexOfFirst { it.allocation == null }
        if (missing >= 0) {
            return AllocationProblem(
                "missing allocation for product '${products[missing].product.name}' (process: ${process.name})",
                missing,
            )
        }

        val first = products.first()
        val dimension = first.allocation!!.unit.dimension
        val other = products.indexOfFirst { it.allocation!!.unit.dimension != dimension }
        if (other >= 0) {
            val product = products[other]
            return AllocationProblem(
                "incompatible dimensions: allocation of product '${product.product.name}' is " +
                    "${product.allocation!!.unit.dimension}, allocation of product '${first.product.name}' is " +
                    "$dimension (process: ${process.name})",
                other,
            )
        }

        if (products.sumOf { weightOf(it) } == 0.0) {
            return AllocationProblem("the allocations of the products sum to zero (process: ${process.name})")
        }
        return null
    }

    private fun weightOf(exchange: TechnoExchangeValue<Q>): Double {
        with(quantityOps) {
            with(ops) {
                return exchange.allocation?.absoluteScaleValue()?.toDouble() ?: 1.0
            }
        }
    }

    private fun applyAllocationToInput(ratio: Double): (TechnoExchangeValue<Q>) -> TechnoExchangeValue<Q> {
        return { technoExchangeValue: TechnoExchangeValue<Q> ->
            technoExchangeValue.copy(
                quantity = technoExchangeValue.quantity.copy(
                    amount = with(ops) { technoExchangeValue.quantity.amount * pure(ratio) }
                )
            )
        }
    }

    private fun applyAllocationToBioExchange(ratio: Double): (BioExchangeValue<Q>) -> BioExchangeValue<Q> {
        return { bioExchange: BioExchangeValue<Q> ->
            bioExchange.copy(
                quantity = bioExchange.quantity.copy(
                    amount = with(ops) { bioExchange.quantity.amount * pure(ratio) }
                ),
            )
        }
    }

    private fun applyAllocationToImpact(ratio: Double): (ImpactValue<Q>) -> ImpactValue<Q> {
        return { impactValue ->
            impactValue.copy(
                quantity = impactValue.quantity.copy(
                    amount = with(ops) { impactValue.quantity.amount * pure(ratio) }
                )
            )
        }
    }
}
