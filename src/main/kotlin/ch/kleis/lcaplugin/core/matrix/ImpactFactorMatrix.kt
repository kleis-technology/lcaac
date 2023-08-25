package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.value.CharacterizationFactorValue
import ch.kleis.lcaplugin.core.lang.value.GenericExchangeValue
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.math.Operations


class ImpactFactorMatrix<Q, M>(
    val observablePorts: IndexedCollection<MatrixColumnIndex<Q>>,
    val controllablePorts: IndexedCollection<MatrixColumnIndex<Q>>,
    private val data: M,
    private val ops: Operations<Q, M>,
) {
    fun value(outputPort: MatrixColumnIndex<Q>, inputPort: MatrixColumnIndex<Q>): CharacterizationFactorValue<Q> {
        with(ops) {
            val outputUnit = outputPort.getDimension().getDefaultUnitValue<Q>()
            val output = GenericExchangeValue(
                QuantityValue(ops.pure(1.0), outputUnit),
                outputPort
            )

            val inputUnit = inputPort.getDimension().getDefaultUnitValue<Q>()
            val amount = data[
                observablePorts.indexOf(outputPort),
                controllablePorts.indexOf(inputPort),
            ]
            val input = GenericExchangeValue(
                QuantityValue(amount, inputUnit),
                inputPort
            )

            return CharacterizationFactorValue(output, input)
        }
    }

    fun valueRatio(outputPort: MatrixColumnIndex<Q>, inputPort: MatrixColumnIndex<Q>): QuantityValue<Q> {
        with(ops) {
            val cf = value(outputPort, inputPort)
            val input = cf.input
            val output = cf.output
            val numerator = input.quantity().amount / pure(inputPort.referenceUnit().scale)
            val denominator = output.quantity().amount / pure(outputPort.referenceUnit().scale)
            return QuantityValue(numerator / denominator, inputPort.referenceUnit())
        }
    }
    
    fun rowAsMap(outputPort: MatrixColumnIndex<Q>): Map<MatrixColumnIndex<Q>, QuantityValue<Q>> {
        return controllablePorts.getElements().associateWith { this.valueRatio(outputPort, it) }
    }

    fun nbCells(): Int {
        return observablePorts.size() * controllablePorts.size()
    }


}
