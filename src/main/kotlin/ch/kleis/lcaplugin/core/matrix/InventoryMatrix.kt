package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.value.CharacterizationFactorValue
import ch.kleis.lcaplugin.core.lang.value.GenericExchangeValue
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.matrix.impl.Matrix



class InventoryMatrix(
    val observablePorts: IndexedCollection<MatrixColumnIndex>,
    val controllablePorts: IndexedCollection<MatrixColumnIndex>,
    private val data: Matrix
) {
    fun value(outputPort: MatrixColumnIndex, inputPort: MatrixColumnIndex): CharacterizationFactorValue {
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

    fun row(outputPort: MatrixColumnIndex): List<CharacterizationFactorValue> {
        return controllablePorts.getElements().map {
            this.value(outputPort, it)
        }
    }
}
