package ch.kleis.lcaac.core.matrix

import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.QuantityValueOperations
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.Operations


class ImpactFactorMatrix<Q, M>(
    val observablePorts: IndexedCollection<MatrixColumnIndex<Q>>,
    val controllablePorts: IndexedCollection<MatrixColumnIndex<Q>>,
    private val data: M,
    private val ops: Operations<Q, M>,
) {
    fun characterizationFactor(
        outputPort: MatrixColumnIndex<Q>,
        inputPort: MatrixColumnIndex<Q>
    ): QuantityValue<Q> {
        with(ops) {
            return when {
                observablePorts.contains(outputPort) -> {
                    val rawRatio = data[
                        observablePorts.indexOf(outputPort),
                        controllablePorts.indexOf(inputPort),
                    ]
                    val ratio = rawRatio * pure(outputPort.referenceUnit().scale / inputPort.referenceUnit().scale)
                    QuantityValue(ratio, inputPort.referenceUnit() / outputPort.referenceUnit())
                }

                controllablePorts.contains(outputPort) && outputPort == inputPort -> {
                    QuantityValue(ops.pure(1.0), UnitValue.none())
                }

                controllablePorts.contains(outputPort) && outputPort != inputPort -> {
                    QuantityValue(ops.pure(0.0), UnitValue.none())
                }

                else -> throw IllegalStateException()
            }
        }
    }

    fun unitaryImpact(outputPort: MatrixColumnIndex<Q>, inputPort: MatrixColumnIndex<Q>): QuantityValue<Q> {
        val quantity = QuantityValue(ops.pure(1.0), outputPort.referenceUnit())
        val factor = characterizationFactor(outputPort, inputPort)
        with(QuantityValueOperations(ops)) {
            return quantity * factor
        }
    }

    fun unitaryImpacts(outputPort: MatrixColumnIndex<Q>): Map<MatrixColumnIndex<Q>, QuantityValue<Q>> {
        return controllablePorts.getElements().associateWith { this.unitaryImpact(outputPort, it) }
    }

    fun nbCells(): Int {
        return observablePorts.size() * controllablePorts.size()
    }


}
