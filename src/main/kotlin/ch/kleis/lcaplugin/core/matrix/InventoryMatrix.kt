package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.matrix.impl.Matrix


sealed interface InventoryResult

class InventoryError(val message: String) : InventoryResult
class InventoryMatrix(
    val observablePorts: IndexedCollection<PortValue>,
    val controllablePorts: IndexedCollection<PortValue>,
    private val data: Matrix
) : InventoryResult {
    fun value(outputPort: PortValue, inputPort: PortValue): CharacterizationFactorValue {
        val outputUnit = defaultUnitOf(outputPort.getDimension())
        val output = GenericExchangeValue(
            QuantityValue(1.0, outputUnit),
            outputPort
        )

        val inputUnit = defaultUnitOf(inputPort.getDimension())
        val sign = if (outputPort is SubstanceValue) -1 else 1
        val amount = sign * data.value(
            observablePorts.indexOf(outputPort),
            controllablePorts.indexOf(inputPort),
        )
        val input = GenericExchangeValue(
            QuantityValue(amount, inputUnit),
            inputPort
        )

        return CharacterizationFactorValue(output, input)
    }

    private fun defaultUnitOf(dim: Dimension): UnitValue {
        return UnitValue("default($dim)", 1.0, dim)
    }
}
