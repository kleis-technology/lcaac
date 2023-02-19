package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.VCharacterizationFactor
import ch.kleis.lcaplugin.core.lang.VExchange
import ch.kleis.lcaplugin.core.lang.VProduct
import ch.kleis.lcaplugin.core.lang.VQuantity
import ch.kleis.lcaplugin.core.matrix.impl.Matrix


sealed interface InventoryResult

class InventoryError(val message: String) : InventoryResult
class InventoryMatrix(
    val observableProducts: IndexedCollection<VProduct>,
    val controllableProducts: IndexedCollection<VProduct>,
    private val data: Matrix
) : InventoryResult {
    fun value(outputProduct: VProduct, inputProduct: VProduct): VCharacterizationFactor {
        val outputReferenceUnit = outputProduct.referenceUnit
        val output = VExchange(VQuantity(1.0, outputReferenceUnit), outputProduct)

        val inputReferenceUnit = inputProduct.referenceUnit
        val amount = data.value(
            observableProducts.indexOf(outputProduct),
            controllableProducts.indexOf(inputProduct),
        )
        val input = VExchange(VQuantity(amount, inputReferenceUnit), inputProduct)

        return VCharacterizationFactor(output, input)
    }
}
