package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.matrix.impl.Matrix
import ch.kleis.lcaplugin.core.matrix.impl.MatrixFactory


class ObservableMatrix(
    private val processes: Collection<ProcessValue>,
    private val substanceCharacterizations: Collection<SubstanceCharacterizationValue>,

    private val observableProducts: Collection<ProductValue>,
    private val observableSubstances: Collection<SubstanceValue>,
) {
    private val connections: IndexedCollection<ConnectionValue> = IndexedCollection(processes.plus(substanceCharacterizations))
    private val ports: IndexedCollection<PortValue> = IndexedCollection(observableProducts.plus(observableSubstances))
    val matrix: Matrix = MatrixFactory.INSTANCE.zero(connections.size(), ports.size())

    init {
        processes.forEach { process ->
            val row = connections.indexOf(process)
            process.products
                .filter { ports.contains(it.product) }
                .forEach {
                    val col = ports.indexOf(it.product)
                    matrix.add(row, col, it.quantity.referenceValue())
                }

            process.inputs
                .filter { ports.contains(it.product) }
                .forEach {
                    val col = ports.indexOf(it.product)
                    matrix.add(row, col, -it.quantity.referenceValue())
                }

            process.biosphere
                .filter { ports.contains(it.substance) }
                .forEach {
                    val col = ports.indexOf(it.substance)
                    matrix.add(row, col, -it.quantity.referenceValue())
                }
        }

        substanceCharacterizations.forEach { characterization ->
            val row = connections.indexOf(characterization)

            listOf(characterization.referenceExchange)
                .filter { ports.contains(it.substance) }
                .forEach {
                    val col = ports.indexOf(it.substance)
                    matrix.add(row, col, it.quantity.referenceValue())
                }

            characterization.impacts
                .filter { ports.contains(it.indicator) }
                .forEach {
                    val col = ports.indexOf(it.indicator)
                    matrix.add(row, col, -it.quantity.referenceValue())
                }
        }
    }
}
