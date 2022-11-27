package ch.kleis.lcaplugin.lib.matrix

import ch.kleis.lcaplugin.lib.matrix.impl.Matrix
import ch.kleis.lcaplugin.lib.model.CharacterizationFactor
import ch.kleis.lcaplugin.lib.model.Exchange
import ch.kleis.lcaplugin.lib.model.Flow
import ch.kleis.lcaplugin.lib.registry.IndexedCollection
import tech.units.indriya.quantity.Quantities.getQuantity
import javax.measure.Quantity

class InventoryMatrix(
    val observableFlows: IndexedCollection<Flow<*>>,
    val controllableFlows: IndexedCollection<Flow<*>>,
    private val data: Matrix
) {
    fun <Din : Quantity<Din>, Dout : Quantity<Dout>> value(
        outputFlow: Flow<Dout>,
        inputFlow: Flow<Din>,
    ): CharacterizationFactor {
        val output = Exchange(outputFlow, getQuantity(1.0, outputFlow.getUnit().systemUnit))
        val amount = data.value(
            observableFlows.indexOf(outputFlow),
            controllableFlows.indexOf(inputFlow)
        )
        val quantity = getQuantity(amount, inputFlow.getUnit().systemUnit)
        val input = Exchange(inputFlow, quantity)
        return CharacterizationFactor(output, input)
    }
}
