package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.value.CharacterizationFactorValue
import ch.kleis.lcaplugin.core.lang.value.GenericExchangeValue
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.math.QuantityOperations
import ch.kleis.lcaplugin.core.matrix.impl.Matrix


class ImpactFactorMatrix<Q>(
    val observablePorts: IndexedCollection<MatrixColumnIndex<Q>>,
    val controllablePorts: IndexedCollection<MatrixColumnIndex<Q>>,
    private val data: Matrix,
    private val ops: QuantityOperations<Q>,
) {
    fun value(outputPort: MatrixColumnIndex<Q>, inputPort: MatrixColumnIndex<Q>): CharacterizationFactorValue<Q> {
        val outputUnit = outputPort.getDimension().getDefaultUnitValue<Q>()
        val output = GenericExchangeValue(
            QuantityValue(ops.pure(1.0), outputUnit),
            outputPort
        )

        val inputUnit = inputPort.getDimension().getDefaultUnitValue<Q>()
        val amount = data.value(
            observablePorts.indexOf(outputPort),
            controllablePorts.indexOf(inputPort),
        )
        val input = GenericExchangeValue(
            QuantityValue(ops.pure(amount), inputUnit),
            inputPort
        )

        return CharacterizationFactorValue(output, input)
    }

    fun valueRatio(outputPort: MatrixColumnIndex<Q>, inputPort: MatrixColumnIndex<Q>): QuantityValue<Q> {
        val cf = value(outputPort, inputPort)
        val input = cf.input
        val output = cf.output
        val numerator = input.quantity().referenceValue(ops) / inputPort.referenceUnit().scale
        val denominator = output.quantity().referenceValue(ops) / outputPort.referenceUnit().scale
        return QuantityValue(ops.pure(numerator / denominator), inputPort.referenceUnit())
    }
    
    fun rowAsMap(outputPort: MatrixColumnIndex<Q>): Map<MatrixColumnIndex<Q>, QuantityValue<Q>> {
        return controllablePorts.getElements().associateWith { this.valueRatio(outputPort, it) }
    }

    fun nbCells(): Int {
        return observablePorts.size() * controllablePorts.size()
    }


}
