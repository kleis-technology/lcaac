package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.core.math.QuantityOperations
import ch.kleis.lcaplugin.core.matrix.impl.Matrix
import ch.kleis.lcaplugin.core.matrix.impl.MatrixFactory


class ObservableMatrix<Q>(
    processes: Collection<ProcessValue<Q>>,
    substanceCharacterizations: Collection<SubstanceCharacterizationValue<Q>>,

    observableProducts: Collection<ProductValue<Q>>,
    observableSubstances: Collection<SubstanceValue<Q>>,

    ops: QuantityOperations<Q>,
) {
    private val connections: IndexedCollection<MatrixRowIndex<Q>> =
        IndexedCollection(processes.plus(substanceCharacterizations))
    private val ports: IndexedCollection<MatrixColumnIndex<Q>> =
        IndexedCollection(observableProducts.plus(observableSubstances))
    val matrix: Matrix = MatrixFactory.INSTANCE.zero(connections.size(), ports.size())

    init {
        processes.forEach { process ->
            val row = connections.indexOf(process)
            process.products
                .filter { ports.contains(it.product) }
                .forEach {
                    val col = ports.indexOf(it.product)
                    matrix.add(row, col, it.quantity.referenceValue(ops))
                }

            process.inputs
                .filter { ports.contains(it.product) }
                .forEach {
                    val col = ports.indexOf(it.product)
                    matrix.add(row, col, -it.quantity.referenceValue(ops))
                }

            process.biosphere
                .filter { ports.contains(it.substance) }
                .forEach {
                    val col = ports.indexOf(it.substance)
                    matrix.add(row, col, -it.quantity.referenceValue(ops))
                }
        }

        substanceCharacterizations.forEach { characterization ->
            val row = connections.indexOf(characterization)

            listOf(characterization.referenceExchange)
                .filter { ports.contains(it.substance) }
                .forEach {
                    val col = ports.indexOf(it.substance)
                    matrix.add(row, col, it.quantity.referenceValue(ops))
                }

            characterization.impacts
                .filter { ports.contains(it.indicator) }
                .forEach {
                    val col = ports.indexOf(it.indicator)
                    matrix.add(row, col, -it.quantity.referenceValue(ops))
                }
        }
    }
}
