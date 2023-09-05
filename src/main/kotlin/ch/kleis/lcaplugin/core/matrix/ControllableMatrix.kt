package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.core.math.Operations

class ControllableMatrix<Q, M>(
    processes: Collection<ProcessValue<Q>>,
    substanceCharacterizations: Collection<SubstanceCharacterizationValue<Q>>,

    terminalProducts: Collection<ProductValue<Q>>,
    terminalSubstances: Collection<SubstanceValue<Q>>,
    indicators: Collection<IndicatorValue<Q>>,

    ops: Operations<Q, M>,
) {
    private val connections: IndexedCollection<MatrixRowIndex<Q>> =
        IndexedCollection(processes.plus(substanceCharacterizations))
    val ports: IndexedCollection<MatrixColumnIndex<Q>> =
        IndexedCollection(terminalProducts.plus(terminalSubstances).plus(indicators))
    val data: M = ops.zeros(connections.size(), ports.size())


    init {
        val quantityOps = QuantityValueOperations(ops)
        with(ops) {
            processes.forEach { process ->
                val row = connections.indexOf(process)
                process.products
                    .filter { ports.contains(it.product) }
                    .forEach {
                        val col = ports.indexOf(it.product)
                        val value = with(quantityOps) { it.quantity.absoluteScaleValue() }
                        data[row, col] = data[row, col] + value
                    }

                process.inputs
                    .filter { ports.contains(it.product) }
                    .forEach {
                        val col = ports.indexOf(it.product)
                        val value = with(quantityOps) { it.quantity.absoluteScaleValue() }
                        data[row, col] = data[row, col] - value
                    }

                process.biosphere
                    .filter { ports.contains(it.substance) }
                    .forEach {
                        val col = ports.indexOf(it.substance)
                        val value = with(quantityOps) { it.quantity.absoluteScaleValue() }
                        data[row, col] = data[row, col] - value
                    }

                process.impacts
                    .filter { ports.contains(it.indicator) }
                    .forEach {
                        val col = ports.indexOf(it.indicator)
                        val value = with(quantityOps) { it.quantity.absoluteScaleValue() }
                        data[row, col] = data[row, col] - value
                    }
            }

            substanceCharacterizations.forEach { characterization ->
                val row = connections.indexOf(characterization)

                listOf(characterization.referenceExchange)
                    .filter { ports.contains(it.substance) }
                    .forEach {
                        val col = ports.indexOf(it.substance)
                        val value = with(quantityOps) { it.quantity.absoluteScaleValue() }
                        data[row, col] = data[row, col] + value
                    }

                characterization.impacts
                    .filter { ports.contains(it.indicator) }
                    .forEach {
                        val col = ports.indexOf(it.indicator)
                        val value = with(quantityOps) { it.quantity.absoluteScaleValue() }
                        data[row, col] = data[row, col] - value
                    }
            }
        }
    }
}
