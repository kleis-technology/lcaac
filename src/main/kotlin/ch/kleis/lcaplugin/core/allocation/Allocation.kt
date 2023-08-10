package ch.kleis.lcaplugin.core.allocation

import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.*
import kotlin.math.absoluteValue

object Allocation {
    fun apply(system: SystemValue): SystemValue {
        val processes = system.processes.flatMap { processValue ->
            if (processValue.products.size > 1) {
                processValue.products.map { allocateProduct(it, processValue) }
            } else {
                listOf(processValue)
            }
        }.toMutableSet()
        return SystemValue(processes, system.substanceCharacterizations)
    }

    private fun allocateProduct(technoExchangeValue: TechnoExchangeValue, processValue: ProcessValue): ProcessValue {
        val totalAllocation = totalAmount(processValue)
        return ProcessValue(
            name = processValue.name,
            labels = processValue.labels,
            products = listOf(technoExchangeValue.copy(allocation = technoExchangeValue.allocation.copy(amount = 100.0))),
            inputs = processValue.inputs.map(applyAllocationToInput(technoExchangeValue.allocation, totalAllocation)),
            biosphere = processValue.biosphere.map(applyAllocationToBioExchange(technoExchangeValue.allocation, totalAllocation)),
            impacts = processValue.impacts.map(applyAllocationToImpact(technoExchangeValue.allocation, totalAllocation))
        )
    }

    fun totalAmount(processValue: ProcessValue): Double {
        allocationUnitCheck(processValue)
        return processValue.products.sumOf { it.allocation.referenceValue() }
    }

    fun allocationUnitCheck(processValue: ProcessValue) {
        if (processValue.products.any { it.allocation.unit.symbol != UnitSymbol.of("percent") }) {
            throw EvaluatorException("Only percent is allowed for allocation unit (process: ${processValue.name})")
        }
        if ((totalAllocationAmounts(processValue) - 100).absoluteValue > 10E-3) {
            throw EvaluatorException("The sum of the allocations should be hundred percent (process: ${processValue.name})")
        }
    }

    private fun totalAllocationAmounts(processValue: ProcessValue): Double {
        return processValue.products.sumOf { it.allocation.amount }
    }

    private fun applyAllocationToInput(
        allocation: QuantityValue,
        totalAllocation: Double
    ): (TechnoExchangeValue) -> TechnoExchangeValue {
        val ratio = allocation.referenceValue() / totalAllocation
        return { technoExchangeValue: TechnoExchangeValue ->
            technoExchangeValue.copy(
                quantity = technoExchangeValue.quantity.copy(
                    amount = technoExchangeValue.quantity.amount * ratio
                )
            )
        }
    }

    private fun applyAllocationToBioExchange(
        allocation: QuantityValue,
        totalAllocation: Double
    ): (BioExchangeValue) -> BioExchangeValue {
        val ratio = allocation.referenceValue() / totalAllocation
        return { bioExchange: BioExchangeValue ->
            bioExchange.copy(
                quantity = bioExchange.quantity.copy(
                    amount = bioExchange.quantity.amount * ratio
                ),
            )
        }
    }

    private fun applyAllocationToImpact(
        allocation: QuantityValue,
        totalAllocation: Double,
    ): (ImpactValue) -> ImpactValue {
        val ratio = allocation.referenceValue() / totalAllocation
        return { impactValue ->
            impactValue.copy(quantity = impactValue.quantity.copy(
                amount = impactValue.quantity.amount * ratio
            ))
        }
    }
}
