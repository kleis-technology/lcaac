package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.value.CharacterizationFactorValue
import ch.kleis.lcaplugin.core.lang.value.GenericExchangeValue
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.matrix.impl.Matrix


class InventoryMatrix(
    val observablePorts: IndexedCollection<MatrixColumnIndex>,
    val controllablePorts: IndexedCollection<MatrixColumnIndex>,
    private val data: Matrix,
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

    fun valueRatio(outputPort: MatrixColumnIndex, inputPort: MatrixColumnIndex): QuantityValue {
        val cf = value(outputPort, inputPort)
        val input = cf.input
        val output = cf.output
        val numerator = input.quantity().referenceValue() / inputPort.referenceUnit().scale
        val denominator = output.quantity().referenceValue() / outputPort.referenceUnit().scale
        return QuantityValue(numerator / denominator, inputPort.referenceUnit())
    }
    
    fun rowAsMap(outputPort: MatrixColumnIndex): Map<MatrixColumnIndex, QuantityValue> {
        return controllablePorts.getElements().associateWith { this.valueRatio(outputPort, it) }
    }

    fun nbCells(): Int {
        return observablePorts.size() * controllablePorts.size()
    }


}
