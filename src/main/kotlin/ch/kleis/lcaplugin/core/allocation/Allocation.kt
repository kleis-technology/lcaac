package ch.kleis.lcaplugin.core.allocation

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.*

class Allocation {
    fun apply(system: SystemValue): SystemValue {
        val processes = system.processes.flatMap { processValue ->
            processValue.products.map { allocateProduct(it, processValue) }
        }.toSet()
        return SystemValue(processes, system.substanceCharacterizations)
    }

    private fun allocateProduct(technoExchangeValue: TechnoExchangeValue, processValue: ProcessValue): ProcessValue {
        val totalAllocation = totalAmount(processValue)
        return ProcessValue(
            processValue.name,
            listOf(technoExchangeValue),
            applyAllocationToInputs(processValue.inputs, technoExchangeValue.allocation, totalAllocation),
            applyAllocationToBioSphere(processValue.biosphere, technoExchangeValue.allocation, totalAllocation)
        )
    }

    fun totalAmount(processValue: ProcessValue): Double {
        allocationUnitCheck(processValue)
        return processValue.products.sumOf { it.allocation.referenceValue() }
    }

    fun allocationUnitCheck(processValue: ProcessValue) {
        if (processValue.products.map { it.allocation.unit.dimension }.distinct().count() > 1) {
            throw EvaluatorException("non-consistent allocation units for process ${processValue.name}")
        }
    }

    private fun applyAllocationToInputs(
        inputs: List<TechnoExchangeValue>,
        allocation: QuantityValue,
        totalAllocation: Double
    ): List<TechnoExchangeValue> {
        return inputs.map { applyAllocationToInput(it, allocation, totalAllocation) }
    }

    private fun applyAllocationToInput(
        technoExchangeValue: TechnoExchangeValue,
        allocation: QuantityValue,
        totalAllocation: Double
    ): TechnoExchangeValue {
        return TechnoExchangeValue(
            QuantityValue(
                technoExchangeValue.quantity.amount * allocation.referenceValue() / totalAllocation,
                technoExchangeValue.quantity.unit
            ),
            technoExchangeValue.product,
            technoExchangeValue.allocation
        )
    }

    private fun applyAllocationToBioSphere(
        biosphere: List<BioExchangeValue>,
        allocation: QuantityValue,
        totalAllocation: Double
    ): List<BioExchangeValue> {
        return biosphere.map { applyAllocationToBioExchange(it, allocation, totalAllocation) }
    }

    private fun applyAllocationToBioExchange(
        bioExchange: BioExchangeValue,
        allocation: QuantityValue,
        totalAllocation: Double
    ): BioExchangeValue {
        return BioExchangeValue(
            QuantityValue(
                bioExchange.quantity.amount * allocation.referenceValue() / totalAllocation,
                bioExchange.quantity.unit
            ),
            bioExchange.substance
        )
    }
}