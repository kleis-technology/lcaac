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
    fun characterizationFactor(
        outputPort: MatrixColumnIndex<Q>,
        inputPort: MatrixColumnIndex<Q>
    ): CharacterizationFactorValue<Q> {
        with(ops) {
            return when {
                observablePorts.contains(outputPort) -> {
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
                    CharacterizationFactorValue(output, input)
                }

                // TODO: Test me
                controllablePorts.contains(outputPort) && outputPort == inputPort -> {
                    CharacterizationFactorValue(
                        GenericExchangeValue(
                            QuantityValue(ops.pure(1.0),inputPort.referenceUnit()),
                            inputPort,
                        ),
                        GenericExchangeValue(
                            QuantityValue(ops.pure(1.0),inputPort.referenceUnit()),
                            inputPort,
                        ),
                    )
                }

                // TODO: Test me
                controllablePorts.contains(outputPort) && outputPort != inputPort -> {
                    CharacterizationFactorValue(
                        GenericExchangeValue(
                            QuantityValue(ops.pure(1.0),inputPort.referenceUnit()),
                            inputPort,
                        ),
                        GenericExchangeValue(
                            QuantityValue(ops.pure(0.0),inputPort.referenceUnit()),
                            inputPort,
                        ),
                    )
                }

                else -> throw IllegalStateException()
            }
        }
    }

    @Deprecated("remove me")
    fun unitaryImpact(outputPort: MatrixColumnIndex<Q>, inputPort: MatrixColumnIndex<Q>): QuantityValue<Q> {
        with(ops) {
            val cf = characterizationFactor(outputPort, inputPort)
            val input = cf.input
            val output = cf.output
            val numerator = absoluteScaleValue(ops, input.quantity()) / pure(inputPort.referenceUnit().scale)
            val denominator = absoluteScaleValue(ops, output.quantity()) / pure(outputPort.referenceUnit().scale)
            return QuantityValue(numerator / denominator, inputPort.referenceUnit())
        }
    }

    @Deprecated("remove me")
    fun rowAsMap(outputPort: MatrixColumnIndex<Q>): Map<MatrixColumnIndex<Q>, QuantityValue<Q>> {
        return controllablePorts.getElements().associateWith { this.unitaryImpact(outputPort, it) }
    }

    fun nbCells(): Int {
        return observablePorts.size() * controllablePorts.size()
    }


}
