package ch.kleis.lcaplugin.compute.matrix

import ch.kleis.lcaplugin.compute.matrix.impl.Matrix
import ch.kleis.lcaplugin.compute.model.Indicator
import ch.kleis.lcaplugin.compute.model.IntermediaryFlow
import tech.units.indriya.quantity.Quantities.getQuantity
import tech.units.indriya.unit.Units.KILOGRAM
import javax.measure.Quantity

class ObservableFactorMatrix(
    private val observableFlows: IndexedCollection<IntermediaryFlow>,
    private val indicators: IndexedCollection<Indicator>,
    val matrix: Matrix
) {
    fun value(flow: IntermediaryFlow, indicator: Indicator): Quantity<*> {
        return getQuantity(1.0, KILOGRAM)
    }
}
