package ch.kleis.lcaplugin.compute.matrix

import ch.kleis.lcaplugin.compute.matrix.impl.Matrix
import ch.kleis.lcaplugin.compute.model.CharacterizationFactor
import ch.kleis.lcaplugin.compute.model.Exchange
import ch.kleis.lcaplugin.compute.model.Flow
import tech.units.indriya.quantity.Quantities.getQuantity
import javax.measure.Quantity

sealed interface InventoryResult

class InventoryError(val message: String) : InventoryResult

class InventoryMatrix(
    val observableFlows: IndexedCollection<Flow<*>>,
    val controllableFlows: IndexedCollection<Flow<*>>,
    private val data: Matrix
) : InventoryResult {
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
