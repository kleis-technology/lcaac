package ch.kleis.lcaplugin.core.allocation

import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.core.math.QuantityOperations
import kotlin.math.absoluteValue

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
        val totalAllocation = totalAmount(processValue)
        return ProcessValue(
            name = processValue.name,
            labels = processValue.labels,
            products = listOf(technoExchangeValue.copy(allocation = technoExchangeValue.allocation)),
            inputs = processValue.inputs.map(
                applyAllocationToInput(
                    allocationOf(technoExchangeValue),
                    totalAllocation
                )
            ),
            biosphere = processValue.biosphere.map(
                applyAllocationToBioExchange(
                    allocationOf(technoExchangeValue),
                    totalAllocation
                )
            ),
            impacts = processValue.impacts.map(
                applyAllocationToImpact(
                    allocationOf(technoExchangeValue),
                    totalAllocation
                )
            )
        )
    }

    fun totalAmount(processValue: ProcessValue<Q>): Double {
        allocationUnitCheck(processValue)
        return processValue.products.sumOf { allocationOf(it) }
    }

    private fun allocationOf(exchange: TechnoExchangeValue<Q>): Double {
        with(quantityOps) {
            with(ops) {
                return exchange.allocation?.absoluteScaleValue()?.toDouble() ?: 1.0
            }
        }
    }

    fun allocationUnitCheck(processValue: ProcessValue<Q>) {
        if (processValue.products
                .mapNotNull { it.allocation }
                .any { it.unit.symbol != UnitSymbol.of("percent") }
        ) {
            throw EvaluatorException("Only percent is allowed for allocation unit (process: ${processValue.name})")
        }
        if ((totalAllocationAmounts(processValue) - 1.0).absoluteValue > 1E-3) {
            throw EvaluatorException("The sum of the allocations should be hundred percent (process: ${processValue.name})")
        }
    }

    private fun totalAllocationAmounts(processValue: ProcessValue<Q>): Double {
        return processValue.products.sumOf { allocationOf(it) }
    }

    private fun applyAllocationToInput(
        allocation: Double,
        totalAllocation: Double
    ): (TechnoExchangeValue<Q>) -> TechnoExchangeValue<Q> {
        val ratio = allocation / totalAllocation
        return { technoExchangeValue: TechnoExchangeValue<Q> ->
            technoExchangeValue.copy(
                quantity = technoExchangeValue.quantity.copy(
                    amount = with(ops) { technoExchangeValue.quantity.amount * pure(ratio) }
                )
            )
        }
    }

    private fun applyAllocationToBioExchange(
        allocation: Double,
        totalAllocation: Double
    ): (BioExchangeValue<Q>) -> BioExchangeValue<Q> {
        val ratio = allocation / totalAllocation
        return { bioExchange: BioExchangeValue<Q> ->
            bioExchange.copy(
                quantity = bioExchange.quantity.copy(
                    amount = with(ops) { bioExchange.quantity.amount * pure(ratio) }
                ),
            )
        }
    }

    private fun applyAllocationToImpact(
        allocation: Double,
        totalAllocation: Double,
    ): (ImpactValue<Q>) -> ImpactValue<Q> {
        val ratio = allocation / totalAllocation
        return { impactValue ->
            impactValue.copy(
                quantity = impactValue.quantity.copy(
                    amount = with(ops) { impactValue.quantity.amount * pure(ratio) }
                )
            )
        }
    }
}
