package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.CharacterizationFactorValue
import ch.kleis.lcaplugin.core.lang.GenericExchangeValue
import ch.kleis.lcaplugin.core.lang.PortValue
import ch.kleis.lcaplugin.core.lang.QuantityValue
import ch.kleis.lcaplugin.core.matrix.impl.Matrix


sealed interface InventoryResult

class InventoryError(val message: String) : InventoryResult
class InventoryMatrix(
    val observablePorts: IndexedCollection<PortValue>,
    val controllablePorts: IndexedCollection<PortValue>,
    private val data: Matrix
) : InventoryResult {
    fun value(outputPort: PortValue, inputPort: PortValue): CharacterizationFactorValue {
        val outputUnit = outputPort.getDimension().getDefaultUnitValue()
        val output = GenericExchangeValue(
            QuantityValue(1.0, outputUnit),
            outputPort
        )

        val inputUnit = inputPort.getDimension().getDefaultUnitValue()
        val amount = data.value(
            observablePorts.indexOf(outputPort),
            controllablePorts.indexOf(inputPort),
        )
        val input = GenericExchangeValue(
            QuantityValue(amount, inputUnit),
            inputPort
        )

        return CharacterizationFactorValue(output, input)
    }
}
